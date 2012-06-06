package jolos.logstashconsumer
import dispatch._ 
import dispatch.liftjson.Js._
import net.liftweb.json.JsonAST._

import akka.actor._
import akka.zeromq._
import akka.serialization.SerializationExtension
import akka.util.Timeout
import akka.pattern.ask
import akka.util.duration._

import play.api.libs.iteratee._
import play.api.libs.concurrent._
import play.api.libs.json._


case object Start
case class Init(sender: ActorRef) extends Message
sealed trait Message
case class RegisterQueryMessage(q: String,index: String, identifier: String) extends Message
// TODO : LogMessage is deprecated
case class LogMessage(m: String) extends Message
case class QueryRegistered(channel: Enumerator[JsValue]) extends Message

/**
 * This object is only used for testing purposes
 **/
object Main {

  implicit val timeout = Timeout(1 second)

  def main(args: Array[String]): Unit = {
    val system = ActorSystem()

    var master = system.actorOf(Props[Master],name = "master")
    val qjson ="{ \"query\" : { \"term\" : { \"field\" : \"value\" } } }"
    val index = "test"
    val identifier = "testquery"
    // register test query
    val msg = (master ? RegisterQueryMessage(qjson,index,identifier)).asPromise.map {
            case m: QueryRegistered =>
              master ! Start 
    }
  }
}

/**
 * The master takes care of starting a Query actor 
 * This is a single point of failure, but doesn't do the heavy lifting so should be ok
 **/
class Master extends Actor with ActorLogging {
  val system = ActorSystem()
  def receive : Receive = {
      case m: RegisterQueryMessage =>
        // create the Query actor
        val query = system.actorOf(Props(new Query(m.q,m.index,m.identifier)), name = m.identifier)
        // initialise it ( register the query to the percolator )
        query ! Init(sender)

        // subscribe to the zmq publisher
        system.newSocket(SocketType.Sub, Listener(query), Connect("tcp://127.0.0.1:5555"), SubscribeAll)
      case Start => 
        log.info("Program started");
      case _ =>
        log.info("received unknown msg");
  }
}

/**
 * Query actor, does all the hard work, 
 * is directly subscribed to the logstash feed ( or anything else, as long as it uses zmq )
 * uses an enumerator to send messages to the user ( most likely a websocket )
 **/
class Query(qjson: String, index: String, identifier: String) extends Actor with ActorLogging {
  var enumerator = Enumerator.imperative[JsValue]()
  def receive : Receive = {
    case m: ZMQMessage => 
      // get the message
      val msg = m.firstFrameAsString;
      // we could create a proper JValue object here, but wrapping with { "doc" : is easier
      val req = url("http://localhost:9200/") / index / "type1" / "_percolate" << "{ \"doc\" : "+msg+"}" 
      val h = new Http
      // do the request to elastic search with dispatch
      val matches=h( req ># { json =>
        // parse the json response
        ( json \\ "matches").values.asInstanceOf[List[String]]
      })

      if( matches contains(identifier)){
        // ok this msg shouln't be filtered out, so let's send it to the client
        enumerator.push(Json.parse(msg))
      } 

    case Init(replyto: ActorRef) =>
      log.info("query initalised")
      val req = url("http://localhost:9200/_percolator/") / index / identifier <<< qjson
      val h = new Http
      // TODO: ignoring the response for now, proper exception handling should happen here
      h(req >|)
      replyto ! QueryRegistered(enumerator)
    case _ => None
  }
}
// vim: set ts=2 sw=2 et:

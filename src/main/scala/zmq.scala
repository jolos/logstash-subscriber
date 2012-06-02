package main.scala
import dispatch._ 
import dispatch.liftjson.Js._
import net.liftweb.json.JsonAST._
import akka.actor._
import akka.zeromq._
import akka.serialization.SerializationExtension
import akka.routing.RoundRobinRouter 


case object Start
case object Init
sealed trait Message
case class RegisterQueryMessage(q: String,index: String, identifier: String) extends Message
case class LogMessage(m: String) extends Message

object Main {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem()

    val listener = system.actorOf(Props(new Actor with ActorLogging {
      // test query

      var queries = Map[String,ActorRef]()

      def receive : Receive = {
        case m: ZMQMessage => 
          queries.foreach( v => (v._2 ! LogMessage(m.firstFrameAsString)));
        case m: LogMessage =>
          queries.foreach( v => ((v._2).!(m)))
        case m: RegisterQueryMessage =>
          val query = system.actorOf(Props(new Query(m.q,m.index,m.identifier)), name = m.identifier)
          queries += (m.identifier -> query)
          query ! Init
        case Start => 
          log.info("Program started");
      }
    }))
    system.newSocket(SocketType.Sub, Listener(listener), Connect("tcp://127.0.0.1:5555"), SubscribeAll)

    listener ! Start
    val qjson ="{ \"query\" : { \"term\" : { \"field\" : \"value\" } } }"
    val index = "test"
    val identifier = "testquery"
    // register test query
    listener ! RegisterQueryMessage(qjson,index,identifier)
    // send test message
    listener ! LogMessage("{ \"doc\" : { \"field\" : \"value\" } }")
  }
}

class Query(qjson: String, index: String, identifier: String) extends Actor with ActorLogging {
  def receive : Receive = {
    case Init  =>
      log.info("query initalised")
      val req = url("http://localhost:9200/_percolator/") / index / identifier <<< qjson
      val h = new Http
      h(req >>> System.out)

    case LogMessage(m: String) => 
      log.info("logmessage received"+ m)
      val req = url("http://localhost:9200/") / index / "type1" / "_percolate" << m
      val h = new Http
      val matches=h( req ># { json =>
        ( json \\ "matches").values.asInstanceOf[List[String]]
      })

      if( matches contains(identifier)){
        log.info( identifier +" contains " + m )
      } else {
        log.info( identifier + " doesn't contain" + m )
      }
      //var map = scala.util.parsing.json.JSON.parseFull(m)
      //log.info(map.toString)
    case Start => log.info("Query actor started")
    case _ => None
  }
}
// vim: set ts=2 sw=2 et:

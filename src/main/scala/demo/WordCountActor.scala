package demo

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberEvent, MemberUp}
import com.typesafe.config.ConfigFactory
import slick.jdbc.GetResult
import slick.jdbc.SQLiteProfile.api._

import scala.collection.{immutable, mutable}
import scala.concurrent.ExecutionContext.Implicits.global

class WordCountActor(id: Int, refs: mutable.Set[ActorRef]) extends Actor with ActorLogging {

  private var received = 0
  private var ups = 1
  private val wordCount = mutable.Map.empty[String, Int] withDefaultValue 0

  private val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberEvent])

  override def postStop(): Unit = cluster.unsubscribe(self)

  private def connectDB(): Unit = {
    val db = Database.forConfig("db")
    val query = sql"SELECT Text FROM demo WHERE ID = $id".as[String]
    val word = db.run(query)

    word.foreach { xs =>
      val map = xs.head.split(" ").groupBy(s => s).mapValues(_.length)
      refs.head ! Count(map)
    }
  }

  private def aggregate(wc: Map[String, Int]): Unit = {
    wc.foreach { case (s, c) => wordCount += (s -> (wordCount(s) + c)) }
    if (received == refs.size) wordCount.foreach(println)
  }

  def receive = {
    case Count(wc) =>
      received += 1
      aggregate(wc)
    case _: MemberUp =>
      ups += 1
      if (ups == refs.size) connectDB()
  }
}

case class Count(wc: immutable.Map[String, Int])

object WordCountActor {
  val refs = mutable.Set.empty[ActorRef]
  var id = 1

  def main(args: Array[String]): Unit = {
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(
      s"""
        akka.remote.netty.tcp.port=$port
        akka.remote.artery.canonical.port=$port
        """)
      .withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    val ref = system.actorOf(Props(classOf[WordCountActor], id, refs), name = "wordcount")
    id += 1
    refs += ref
  }
}
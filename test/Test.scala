package test

import akka.actor.ActorSystem
import java.sql.Timestamp
import models.DBEvent
import com.typesafe.config.ConfigFactory
import util.Random
import akka.util.duration._

object Test {

  val seglUsers = List("Nicolas","Jean-Luc","Edmondo","Luc","Carlo","Edvīns", "Benoit","François", "Leif","Pascal","Jean")

  def main(args: Array[String]) {

    // Get the actor system using the inline configuration
    // As the port is set to 0, delegate to Akka to pick a free port in case the remote actor wish to send back a message.
    val actorSystem = ActorSystem("client", ConfigFactory.parseString(
      """
        |akka {
        | actor {
        |   provider = "akka.remote.RemoteActorRefProvider"
        | }
        | remote {
        |   transport = "akka.remote.netty.NettyRemoteTransport"
        |   netty {
        |     hostname = "127.0.0.1"
        |     port = 0
        |   }
        |    }
        |}
      """.stripMargin))

    // Get the remote Actor running in Play!
    val client = actorSystem.actorFor("akka://ui@127.0.0.1:20000/user/server-push")

    // Define a Runnable class used by the Akka scheduler to send messages.
    class SendMessage extends Runnable {
      def run() {
        client ! DBEvent(
          Some(new Timestamp(System.currentTimeMillis())),
          Random.nextInt(2000),
          seglUsers(Random.nextInt(seglUsers.size)),
          if(Random.nextBoolean()) 12 else 13)
      }
    }

    actorSystem.log.info("Sending Messages to the server-push actor ...")
    // Thanks to the "import akka.util.duration._" a can easily create Duration
    actorSystem.scheduler.schedule(500 millis, 100 millis, new SendMessage)

    try {
      // Await the timeout defined by the duration
      actorSystem.awaitTermination(60 seconds)
    }
    catch {
      case _ => actorSystem.log.info("Done ! About to shutdown ...")
    } finally {
      // Shutdown the actorSystem otherwise I'll never exit the VM
      actorSystem.shutdown()
    }

  }
}

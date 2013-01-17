package application

import akka.actor.{Props, ActorSystem}
import play.api.{Application, GlobalSettings}
import com.typesafe.config.ConfigFactory
import play.Play
import models.ServerPush

object Global extends GlobalSettings {

  // Externalize some properties
  val AkkaHostnameKey = "akka.hostname"
  val AkkaPortKey = "akka.port"

  private lazy val akkaHostname = Play.application().configuration().getString(AkkaHostnameKey)
  private lazy val akkaPort     = Play.application().configuration().getString(AkkaPortKey)

  // Define the Akka system actor configuration for this UI
  lazy val actorSystem : ActorSystem = ActorSystem("ui", ConfigFactory.parseString(
    """
      |akka {
      |  actor {
      |    provider = "akka.remote.RemoteActorRefProvider"
      |  }
      |  remote {
      |    transport = "akka.remote.netty.NettyRemoteTransport"
      |    netty {
      |      hostname = "%s"
      |      port = %s
      |    }
      |  }
      |}
    """.stripMargin.format(akkaHostname,akkaPort)).withFallback(ConfigFactory.load()))

  lazy val serverPush = actorSystem.actorOf(Props(new ServerPush()),"server-push")


  override def onStart(app: Application) {
  }

  override def onStop(app: Application) {
    actorSystem.shutdown()
  }
}

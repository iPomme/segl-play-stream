package models

import akka.actor.Actor
import collection.immutable.Queue
import play.api.libs.iteratee.{Enumerator, PushEnumerator}
import java.sql.Timestamp

// Events -------------------------

case class DBEvent(
                    when: Option[Timestamp],
                    duration: Long,
                    loginName: String,
                    evtType: Int
                    )

// Actors Messages ----------------
case class Connected(enumerator: Enumerator[DBEvent])
case object Join

// Actors -------------------------
class ServerPush extends Actor {

  // List of all the connected client (a Queue to limit the number)
  var clients: Queue[PushEnumerator[DBEvent]] = Queue()
  val maxClient = 100

  protected def receive: Receive = {
    // A new client is coming
    case Join => {
      // Limit the number of client for this example
      if (clients.size > maxClient) {
        val (e, q) = clients.dequeue
        e.close()
        clients = q
      }
      // Create the Enumeratee
      val channel: PushEnumerator[DBEvent] = Enumerator.imperative[DBEvent]()
      // Save the client
      clients = clients.enqueue(channel)
      // Send back the Enumeratee reference
      sender ! Connected(channel)
    }

    // received new events and send to all the client
    case stats: DBEvent => {
      clients.foreach(_.push(stats))
    }
  }
}

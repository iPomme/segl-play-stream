package test

import play.api.libs.iteratee.{Iteratee, Enumeratee, Enumerator}
import akka.util.duration._
import play.api.libs.concurrent.Promise

object EnumeratorTest {

  def main(args: Array[String]) {
    // Different way to define the source

    // Source as a List
    val numbers: Enumerator[Int] = Enumerator(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    // Definition of an Enumeratee for conversion
    def conv: Enumeratee[Int, String] = Enumeratee.mapInput[Int] {
      case input => {
        input.map(
          i => {
            i.toString + "."
          }
        )
      }
    }

    // Pipe the numbers and the conv
    val a = numbers &> conv

    val iteratee = Iteratee.foreach[String](a => println(Thread.currentThread().getName + ":" +a))
    a(iteratee)


    // Source as a scheduler
    val schedule = Enumerator.fromCallback[Int] {
      () => Promise.timeout({
        Option(System.currentTimeMillis().toInt)
      }, 500 milliseconds)
    }
    val b = schedule &> conv
    b(iteratee)

    //Source as imperative code using push
    val call = Enumerator.imperative[Int]()
    val c = call &> conv
    c(iteratee)

    for (i <- 0 until 100) {
      call.push(i)
    }


  }

}

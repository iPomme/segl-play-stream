package controllers

import play.api.mvc._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.util.duration._
import akka.pattern.ask

import application.Global
import play.api.libs.EventSource.EventNameExtractor
import play.api.libs.json.JsValue
import play.api.libs.EventSource
import models.{Streams, Connected, Join}


object Application extends Controller {


  // Entry point of the application
  def index = Action {
    Ok(views.html.index())
  }

  // The Server event stream definition
  def stream = Action {
    Async {
      implicit val timeout = Timeout(1 second)
      (Global.serverPush ? Join).asPromise.map {
        case Connected(channel) => {
          // Compose the channel to transform it to JSON
          val events = channel &> Streams.toJson
          // Set the event tag name used to specify the event name.
          implicit val eventNameExtractor: EventNameExtractor[JsValue] = EventNameExtractor[JsValue](eventName = (zepEvent) => zepEvent.\("event").asOpt[String])

          Ok.feed(events &> EventSource()).as("text/event-stream")
        }
      }
    }
  }

}
package models

import play.api.libs.iteratee.Enumeratee
import play.api.libs.json.{Json, JsValue}
import controllers.Application._
import java.text.SimpleDateFormat

object Streams {


  /**
   * Convert the class case to JSON
   * @return
   */
  def toJson : Enumeratee[DBEvent, JsValue] = Enumeratee.mapInput[DBEvent] {
    case someMap => {
      someMap.map {
        contentParsed => {

          // Convert everything to String
          val durationString:String = if(contentParsed.duration == -1) "N/A" else contentParsed.duration.toString
          val evtTypeString:String = if(contentParsed.evtType == -1) "N/A" else contentParsed.evtType.toString
          val loginNameString:String = if(contentParsed.loginName.isEmpty) "N/A" else contentParsed.loginName
          val dateFormat = new SimpleDateFormat("HH:mm:ss.SSS")
          val whenString:String = if(contentParsed.when.isDefined) dateFormat.format(contentParsed.when.get)  else "N/A"


          Json.toJson(
            Map(
              "event" -> Json.toJson("dbInfo"), // Used in javascript to register to theses events
              "duration" -> Json.toJson(durationString),
              "evtType" -> Json.toJson(evtTypeString),
              "loginName" -> Json.toJson(loginNameString),
              "when" -> Json.toJson(whenString)
            )
          )
        }
      }
    }
  }
}

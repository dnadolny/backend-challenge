package com.dnadolny.ada.app

import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import org.json4s.DefaultFormats
import org.json4s.JsonDSL._
import org.json4s.MappingException
import org.json4s.jackson.JsonMethods._
import org.json4s._
import org.scalatra._
import org.slf4j.LoggerFactory

import com.dnadolny.ada._
import com.fasterxml.jackson.core.JsonParseException

import javax.servlet.http._

class ConversationAPIServlet(clock: Clock, db: Db) extends ScalatraServlet {
  import ConversationAPIServlet._
  val logger =  LoggerFactory.getLogger(getClass)
  implicit val formats = DefaultFormats
  
  override protected def serveStaticResource()(implicit request: HttpServletRequest, response: HttpServletResponse) = None //never serve static files
  
  implicit override def string2RouteMatcher(path: String) = RailsPathPatternParser(path) //use rails matcher for optional params
    
  post("/messages/") {
    try {
      val json = parse(request.body).camelizeKeys.extract[CreateMessageJSON]
      if (json.conversationId.matches(ValidConversationIdRegex)) {
        db.save(Message(json.conversationId, json.sender, json.message, clock.now()))
        Created("Message created")
      } else {
        badRequestInvalidConversationId(json.conversationId)
      }
    } catch {
      case e: JsonParseException =>
        BadRequest("Error parsing JSON - " + e.getMessage)
      case e: MappingException => 
        BadRequest("Could not extract sender/message/conversation_id - make sure they are all present and the right data type")
    }
  }
  
  get("/conversations/(:conversationId)") {
    val conversationId = params.get("conversationId").getOrElse("")
    if (conversationId.matches(ValidConversationIdRegex)) {
      val messages = db.query[Message].whereEqual("conversationId", conversationId).order("createdAt").limit(MessageSanityLimit).fetch().toList
      if (messages.length > MessageSanityLimit * 0.9) {
        logger.warn("We have a large number of messages for conversationId '$conversationId', we may want to implement pagination")
      }
      val json =
        ("id" -> conversationId) ~
        ("messages" -> messages.map { m =>
          (("sender" -> m.sender) ~
           ("message" -> m.message) ~
           ("created" -> ISODateTimeFormat.dateTime().print(m.createdAt.withZone(DateTimeZone.UTC))))
        })
      compact(render(json))
    } else {
      badRequestInvalidConversationId(conversationId)
    }
  }
  
  private def badRequestInvalidConversationId(conversationId: String) = BadRequest(s"conversation_id must be lowercase alphanumeric, 1 to 16 characters. Instead it was '$conversationId'")
}

object ConversationAPIServlet {
  val ValidConversationIdRegex = """[a-z0-9]{1,16}"""
  val MessageSanityLimit = 10000
}

case class CreateMessageJSON(sender: String, conversationId: String, message: String)

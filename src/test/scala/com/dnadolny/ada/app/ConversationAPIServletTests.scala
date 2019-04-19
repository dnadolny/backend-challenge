package com.dnadolny.ada.app

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization

import org.scalatra.test.scalatest._
import com.dnadolny.ada.FakeClock
import com.dnadolny.ada.DbCredentials
import com.dnadolny.ada.Db

class MyScalatraServletTests extends ScalatraFunSuite {
  implicit val formats = DefaultFormats
  val clock = new FakeClock
  val inMemoryDbCredentials = DbCredentials("jdbc:h2:mem:adabackendchallenge;MODE=PostgreSQL", "sa", "sa")
  addServlet(new ConversationAPIServlet(clock, new Db(inMemoryDbCredentials)), "/*")
  
  test("POST /messages/ returns 201") {
    val validJsonMessage = """{
      "sender": "anson",
      "conversation_id": "1234",
      "message": "I'm a teapot"
    }"""
    postJson("/messages/", validJsonMessage) {
      body should equal ("Message created")
      status should equal (201)
    }
  }
  
  test("POST /messages/ with unparseable JSON gives a nice error message") {
    //invalid because no comma after first item "sender": "anson"
    val invalidJsonMessage = """{
      "sender": "anson"
      "conversation_id": "1234",
      "message": "I'm a teapot"
    }"""
    
    postJson("/messages/", invalidJsonMessage) {
      body should equal ("""Error parsing JSON - Unexpected character ('"' (code 34)): was expecting comma to separate Object entries
 at [Source: {
      "sender": "anson"
      "conversation_id": "1234",
      "message": "I'm a teapot"
    }; line: 3, column: 8]""")
      status should equal (400)
    }
  }
  
  test("POST /messages/ with missing json key gives a nice error message") {
    val messageMissingSender = """{
      "conversation_id": "1234",
      "message": "I'm a teapot"
    }"""
    postJson("/messages/", messageMissingSender) {
      body should equal ("Could not extract sender/message/conversation_id - make sure they are all present and the right data type")
      status should equal (400)
    }
  }
  
  test("POST /messages/ with conversation_id as a boolean gives a nice error message") {
    val conversationIdIsWrongDataType = """{
      "sender": "anson",
      "conversation_id": false,
      "message": "I'm a teapot"
    }"""
    postJson("/messages/", conversationIdIsWrongDataType) {
      body should equal ("Could not extract sender/message/conversation_id - make sure they are all present and the right data type")
      status should equal (400)
    }
  }
  
  test("POST /messages/ with invalid conversation_id (contains symbols) gives a nice error message") {
    val conversationIdContainsSymbols = """{
      "sender": "anson",
      "conversation_id": "1234%",
      "message": "I'm a teapot"
    }"""
    postJson("/messages/", conversationIdContainsSymbols) {
      body should equal ("conversation_id must be lowercase alphanumeric, 1 to 16 characters. Instead it was '1234%'")
      status should equal (400)
    }
  }
  
  test("GET /messages/ returns 405: method not allowed") {
    get("/messages/") {
      status should equal (405)
    }
  }
  
  test("GET /conversations/ after saving one item works") {
    save(CreateMessageJSON("sender_here", "111", "message_here"), "2019-04-19T00:56:00.960Z")
    get("/conversations/111") {
      body should equal ("""{"id":"111","messages":[{"sender":"sender_here","message":"message_here","created":"2019-04-19T00:56:00.960Z"}]}""")
      status should equal (200)
    }
  }
  
  test("GET /conversations/ after saving three items is sorted by created date") {
    save(CreateMessageJSON("sender1", "112", "message1"), "2019-04-19T00:56:00.960Z")
    save(CreateMessageJSON("sender3", "112", "message3"), "2019-04-19T00:58:00.960Z")
    save(CreateMessageJSON("sender2", "112", "message2"), "2019-04-19T00:57:00.960Z")
    get("/conversations/112") {
      val formattedBodyJson = (pretty(render(parse(body))))
      formattedBodyJson should equal ("""{
  "id" : "112",
  "messages" : [ {
    "sender" : "sender1",
    "message" : "message1",
    "created" : "2019-04-19T00:56:00.960Z"
  }, {
    "sender" : "sender2",
    "message" : "message2",
    "created" : "2019-04-19T00:57:00.960Z"
  }, {
    "sender" : "sender3",
    "message" : "message3",
    "created" : "2019-04-19T00:58:00.960Z"
  } ]
}""")
      status should equal (200)
    }
  }
  
  test("GET /conversations/ with alphanumeric conversation id") {
    save(CreateMessageJSON("sender_here", "abc123", "message_here"), "2019-04-19T00:56:00.960Z")
    get("/conversations/abc123") {
      body should equal ("""{"id":"abc123","messages":[{"sender":"sender_here","message":"message_here","created":"2019-04-19T00:56:00.960Z"}]}""")
      status should equal (200)
    }
  }
  
  test("GET /conversations/ with no saved messages results in valid json with empty message array") {
    get("/conversations/113") {
      body should equal ("""{"id":"113","messages":[]}""")
      status should equal (200)
    }
  }
  
  test("GET /conversations/ with invalid conversation id (containing symbols) results in 400") {
    get("/conversations/1234$") {
      body should equal ("conversation_id must be lowercase alphanumeric, 1 to 16 characters. Instead it was '1234$'")
      status should equal (400)
    }
  }
  
  test("GET /conversations/ with invalid conversation id (containing upper case letters) results in 400") {
    get("/conversations/ABC123") {
      body should equal ("conversation_id must be lowercase alphanumeric, 1 to 16 characters. Instead it was 'ABC123'")
      status should equal (400)
    }
  }
  
  test("GET /conversations/ with no conversation id results in 400") {
    get("/conversations/") {
      body should equal ("conversation_id must be lowercase alphanumeric, 1 to 16 characters. Instead it was ''")
      status should equal (400)
    }
  }
  
  test("POST /conversations/ returns 405: method not allowed") {
    post("/conversations/") {
      status should equal (405)
    }
  }
  
  test("GET / returns 404 and lists routes") {
    get("/") {
      body.trim should equal ("""Requesting "GET /" on servlet "" but only have: <ul><li>GET \A/conversations/(?:([^#/.?]+))?\Z</li><li>POST \A/messages/\Z</li></ul>""")
      status should equal (404)
    }
  }
  
  test("GET /asdf returns 404 because it's an undefined route") {
    get("/asdf") {
      status should equal (404)
    }
  }
  
  def postJson[A](uri: String, json: String)(f: => A): A = {
    post(uri, json.getBytes, Map("Content-Type" -> "application/json"))(f)
  }
  
  def save(message: CreateMessageJSON, time: String) {
    clock.setTime(time)
    postJson("/messages/", Serialization.write(message)) {
      status should equal (201)
    }
  }
}

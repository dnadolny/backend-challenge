package com.dnadolny.ada

import sorm._
import org.joda.time.DateTime

case class Message(
  conversationId: String,
  sender: String,
  message: String,
  createdAt: DateTime
)

class Db(credentials: DbCredentials) extends Instance(
  entities = Set(
    Entity[Message]()
  ),
  url = credentials.url,
  user = credentials.user,
  password = credentials.password,
  initMode = InitMode.Create, //really we'd InitMode.DoNothing and use proper versioning like FlyWay, create index on conversationId and createdAt
  poolSize = 10
)

case class DbCredentials(url: String, user: String, password: String)

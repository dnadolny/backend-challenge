package com.dnadolny.ada

import org.joda.time._

trait Clock {
  def now(): DateTime
}

class RealClock extends Clock {
  override def now() = DateTime.now(DateTimeZone.UTC)
}

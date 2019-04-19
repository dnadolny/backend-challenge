package com.dnadolny.ada

import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime

class FakeClock extends Clock {
  var time = ISODateTimeFormat.dateTime().parseDateTime("2019-04-19T00:56:00.960Z")
  
  override def now() = time
  
  def setTime(isoTime: String) = {
    time = ISODateTimeFormat.dateTime().parseDateTime(isoTime)
  }
}

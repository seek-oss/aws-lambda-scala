package tekumara

import ujson.{Obj, Value}

import scala.collection.mutable

object Inputs extends Inputs

trait Inputs {

  object SqsRecords {
    // extract array of Records if the first one is an SQS message
    def unapply(v: Value): Option[mutable.ArrayBuffer[Value]] = {
      v match {
        case obj: Obj if obj.value.contains("Records") &&
          obj("Records").isInstanceOf[ujson.Arr] &&
          obj("Records")(0).obj.value.contains("body") =>
          Some(obj("Records").arr)
        case _ => None
      }
    }
  }

}

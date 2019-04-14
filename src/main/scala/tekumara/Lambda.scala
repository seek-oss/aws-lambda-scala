package tekumara

import java.io.OutputStreamWriter

import com.amazonaws.services.lambda.runtime.Context
import com.typesafe.scalalogging.StrictLogging
import ujson.Value
import Inputs._

class Lambda extends RequestUjsonHandler with StrictLogging {

  logger.info(s"Lambda initialised version ${System.getenv("version")}")

  override def handleRequest(json: Value, writer: OutputStreamWriter, context: Context): Unit = {
    json match {
      case ujson.Str("exit") =>
        System.exit(1)
      case ujson.Str(s) =>
        writer.write(s)
      case SqsRecords(records) =>
        logger.info(s"input=${json.render()}")
        val body = records(0).obj("body")
        logger.info(s"body=${body.render()}")
        ujson.writeTo(body, writer)
      case _ => throw new RuntimeException(s"Unexpected input ${json.render()}")
    }
  }

}

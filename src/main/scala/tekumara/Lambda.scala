package tekumara

import java.io.OutputStreamWriter

import com.amazonaws.services.lambda.runtime.Context
import com.typesafe.scalalogging.StrictLogging
import ujson.Value
import Inputs._
import org.slf4j.MDC

class Lambda extends RequestUjsonHandler with StrictLogging {

  MDC.put("version", System.getenv("version"))
  logger.info(s"Lambda initialised")

  override def handleRequest(json: Value, writer: OutputStreamWriter, context: Context): Unit = {
    json match {
      case ujson.Str("exit") =>
        logger.info("exit")
        System.exit(1)
      case ujson.Str(s) =>
        logger.info(s)
        writer.write(s)
      case SqsRecords(records) =>
        val record = records(0).obj
        logger.info(s"input=${json.render()}")
        val body = record("body")
        logger.info(s"body=${body.render()}")
        ujson.writeTo(body, writer)
      case _ => throw new RuntimeException(s"Unexpected input ${json.render()}")
    }
  }

}

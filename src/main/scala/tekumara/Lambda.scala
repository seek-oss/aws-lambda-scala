package tekumara

import java.io.OutputStreamWriter

import com.amazonaws.services.lambda.runtime.Context
import com.typesafe.scalalogging.StrictLogging
import ujson.Value

class Lambda extends RequestUjsonHandler with StrictLogging {

  logger.info(s"Lambda initialised version ${System.getenv("version")}")

  override def handleRequest(json: Value, writer: OutputStreamWriter, context: Context): Unit = {
    json match {
      case ujson.Str("exit") =>
        System.exit(1)
      case ujson.Str(s) => writer.write(s)
      case _ => throw new RuntimeException(s"Unexpected input ${json.render()}")
    }
  }

}

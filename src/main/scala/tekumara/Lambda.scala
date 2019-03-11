package tekumara

import com.amazonaws.services.lambda.runtime.Context
import com.typesafe.scalalogging.StrictLogging
import ujson.Value

class Lambda extends RequestUjsonHandler with StrictLogging {

  logger.info(s"Lambda initialised version ${System.getenv("version")}")

  override def handleRequest(json: Value, context: Context): Option[Value] = {
    json match {
      case ujson.Str("exit") =>
        System.exit(1); None
      case x@ujson.Str(_) => Some(x)
      case _ => throw new RuntimeException(s"Unexpected input ${json.render()}")
    }
  }

}

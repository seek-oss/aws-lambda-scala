package tekumara

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.typesafe.scalalogging.StrictLogging

class Lambda extends RequestHandler[Any, Any] with StrictLogging {

  logger.info("Lambda initialised")

  override def handleRequest(input: Any, context: Context): Any = {
    logger.info(s"Received ${input.getClass}: ${input.toString}")

    input match {
      case "exit" => System.exit(1)
      case x: String => x
      case _ => throw new RuntimeException(s"Unexpected input ${input.getClass}: ${input.toString}")
    }
  }

}

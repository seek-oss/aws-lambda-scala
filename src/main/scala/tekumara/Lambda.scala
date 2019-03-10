package tekumara

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.typesafe.scalalogging.StrictLogging

class Lambda extends RequestHandler[Any, Any] with StrictLogging {

  logger.info("Lambda initialised")

  override def handleRequest(input: Any, context: Context): Any = {
    logger.info(s"Received $input")
    input
  }

}

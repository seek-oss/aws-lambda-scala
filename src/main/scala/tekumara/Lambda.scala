package tekumara

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.typesafe.scalalogging.StrictLogging

import scala.util.control.NonFatal
import Exceptions._

class Lambda extends RequestHandler[Any, Any] with StrictLogging {

  logger.info(s"Lambda initialised version ${System.getenv("version")}")

  override def handleRequest(input: Any, context: Context): Any = {
    try {
      logger.info(s"Received ${input.getClass}: ${input.toString}")

      input match {
        case "exit" => System.exit(1)
        case x: String => x
        case _ => throw new RuntimeException(s"Unexpected input ${input.getClass}: ${input.toString}")
      }
    } catch {
      case NonFatal(e) =>
        // if we just let the exception be thrown it will be logged without any context
        // we explicitly log it so we have full context, eg: AWSRequestId etc.
        logger.error(e.getLocalizedMessage, filterStackTrace(e))

        // throw error without stack trace so it's not repeated in the logs
        // the error message and class thrown here will be available in the logs and
        // to clients invoking the lambda with RequestResponse
        e.setStackTrace(new Array[StackTraceElement](0))
        throw e
    }
  }

}

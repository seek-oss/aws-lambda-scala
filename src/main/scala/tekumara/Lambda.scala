package tekumara

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.typesafe.scalalogging.StrictLogging

import scala.util.control.NonFatal

class CaughtException(message: String) extends RuntimeException(message)

class Lambda extends RequestHandler[Any, Any] with StrictLogging {

  logger.info("Lambda initialised")

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
        logger.error(e.getLocalizedMessage, e)

        // throw new error without stack trace so it's not repeated in the logs
        // the error message returned here will be available in the logs and
        // to clients using invocation type RequestResponse
        throw new CaughtException(s"Request ${context.getAwsRequestId} threw ${e.getClass.getSimpleName}" +
          s"${if (e.getMessage == null) "" else ": " + e.getMessage}")
    }
  }

}

package tekumara

import java.io.{InputStream, OutputStream, OutputStreamWriter}

import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import com.typesafe.scalalogging.StrictLogging
import tekumara.Exceptions._
import tekumara.Util._

import scala.util.control.NonFatal
import scala.util.{Success, Try}

class Lambda extends RequestStreamHandler with StrictLogging {

  logger.info(s"Lambda initialised version ${System.getenv("version")}")

  override def handleRequest(in: InputStream, out: OutputStream, context: Context): Unit =
    try {
      val input: Array[Byte] = readAllBytes(in)

      Try(ujson.read(input)) match {
        case Success(ujson.Str("exit")) => System.exit(1)
        case Success(x@ujson.Str(_)) =>
          val writer = new OutputStreamWriter(out)
          ujson.writeTo(x, writer)
          writer.flush()
        case _ => throw new RuntimeException(s"Unexpected input ${asTruncatedString(input, 100)}")
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

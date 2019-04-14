package tekumara

import java.io.{ByteArrayOutputStream, InputStream, OutputStream, OutputStreamWriter}

import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import com.typesafe.scalalogging.StrictLogging
import tekumara.Exceptions.filterStackTrace

import scala.util.control.NonFatal

trait RequestUjsonHandler extends RequestStreamHandler with StrictLogging {

  def handleRequest(json: ujson.Value, writer: OutputStreamWriter, context: Context): Unit

  override def handleRequest(in: InputStream, out: OutputStream, context: Context): Unit = {
    try {
      val json = ujson.read(readAllBytes(in))
      val writer = new OutputStreamWriter(out)
      handleRequest(json, writer, context)
      writer.flush()
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

  def readAllBytes(in: InputStream): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    transferTo(in, baos)
    baos.toByteArray
  }

  def transferTo(in: InputStream, out: OutputStream): Unit = {
    val buffer = new Array[Byte](8192)

    while ( {
      in.read(buffer) match {
        case -1 => false
        case n =>
          out.write(buffer, 0, n)
          true
      }
    }) ()
  }

}

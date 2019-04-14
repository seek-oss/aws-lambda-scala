package tekumara

import java.io._
import java.nio.charset.StandardCharsets

import com.amazonaws.services.lambda.runtime.Context
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSuite, Matchers}
import ujson.{ParseException, Value}

class RequestUjsonHandlerTest extends FunSuite with MockitoSugar with Matchers {

  val lambda = new RequestUjsonHandler {
    override def handleRequest(json: Value, writer: OutputStreamWriter, context: Context) = ujson.writeTo(json, writer)
  }

  def asInputStream(s: String) = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8))

  test("happy path - deserialise input, serialise result") {
    val baos = new ByteArrayOutputStream
    val out = new PrintStream(baos, true)

    val input = "\"hello ujson\""
    lambda.handleRequest(asInputStream(input), out, mock[Context])

    baos.toString should be(input)
  }

  test("invalid json input throws exception") {
    val input = "hello world"
    an[ParseException] should be thrownBy lambda.handleRequest(asInputStream(input), mock[OutputStream], mock[Context])
  }

}

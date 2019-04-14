package tekumara

import java.io._
import java.nio.charset.StandardCharsets

import com.amazonaws.services.lambda.runtime.Context
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSuite, Matchers}

class LambdaTest extends FunSuite with MockitoSugar with Matchers {

  val lambda = new Lambda

  def asInputStream(s: String) = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8))

  val sqsPayload = ClassLoader.getSystemResourceAsStream("inputs-sqs.json")

  test("deserialise sqs") {
    val baos = new ByteArrayOutputStream
    val out = new PrintStream(baos, true)

    lambda.handleRequest(sqsPayload, out, mock[Context])

    baos.toString should be(""""\"json-body\""""")
  }

}

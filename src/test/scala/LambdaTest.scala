import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import com.amazonaws.services.lambda.runtime.Context
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSuite, Matchers}
import tekumara.Lambda

class LambdaTest extends FunSuite with MockitoSugar with Matchers {

  val lambda = new Lambda()

  def asInputStream(s: String) = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8))

  test("echo valid input") {
    import java.io.{ByteArrayOutputStream, PrintStream}
    val baos = new ByteArrayOutputStream
    val out = new PrintStream(baos, true)

    val input = "\"hello world\""
    lambda.handleRequest(asInputStream(input), out, mock[Context])

    baos.toString should be (input)
  }

}

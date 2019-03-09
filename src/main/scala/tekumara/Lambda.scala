package tekumara

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

class Lambda extends RequestHandler[Any, Any] {

  override def handleRequest(input: Any, context: Context): Any = {
    input
  }

}

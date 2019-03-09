package tekumara

import com.amazonaws.services.lambda.runtime.Context

class Lambda {

  def handler(event: Any, context: Context): String = {
    "Hello world"
  }

}

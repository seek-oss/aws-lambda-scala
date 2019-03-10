package tekumara

object Exceptions {

  // remove aws lambda runtime stack lines from the stack trace
  def filterStackTrace[T <: Throwable](t: T): T = {

    def removeLambdaInternal(t: T): Unit = {
      val trace = t.getStackTrace

      var i = 0
      while ( {
        i < trace.length
      }) {
        if (trace(i).getClassName.startsWith("lambdainternal")) {
          val newTrace = new Array[StackTraceElement](i)
          System.arraycopy(trace, 0, newTrace, 0, i)
          t.setStackTrace(newTrace)
          return
        }
        i += 1
      }
    }

    removeLambdaInternal(t)
    val cause = t.getCause
    if (cause != null) filterStackTrace(cause)
    t
  }
}

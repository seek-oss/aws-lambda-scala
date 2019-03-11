package tekumara

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}

object Util {

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

  def readAllBytes(in: InputStream): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    Util.transferTo(in, baos)
    baos.toByteArray
  }

  def asTruncatedString(a: Array[Byte], n: Int): String = {
    if (a.length > n) {
      new String(a)
    } else {
      new String(a.take(n)).concat("...")
    }

  }
}

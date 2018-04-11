package fiware

import scala.collection.mutable.ArrayBuffer


object JsonSerializer {
  def serialize(map:Map[String,Any]):String = {
    var buf = new StringBuffer()
    val list = new ArrayBuffer[String]()

    map foreach (x => {
      buf.append("\"").append(x._1).append("\"").append(":")
      if(x._2.isInstanceOf[Map[String,Any]]) {
        list += (buf.append(serialize(x._2.asInstanceOf[Map[String,Any]])).toString)
      }
      else {
        buf.append(f(x._2))
        list += buf.toString
      }

      buf = new StringBuffer()
    })

    s"{\n${list.mkString(",\n")}}"
  }

  def f[T](v: T) = v match {
    case _: String => "\"" + v + "\""
    case _         => v
  }

}

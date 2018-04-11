package fiware

import scala.collection.mutable.ArrayBuffer


object JsonSerializer {
  def serialize(map:Map[String,Any]):String = {
    val list = new ArrayBuffer[String]()

    map foreach (x => {
      val buf = new StringBuffer()
      buf.append("\"").append(x._1).append("\"").append(":")
      if(x._2.isInstanceOf[Map[String,Any]]) {
        list += (buf.append(serialize(x._2.asInstanceOf[Map[String,Any]])).toString)
      }
      else if(x._2.isInstanceOf[List[Map[String,Any]]]) {
        buf.append("[")
        val elementList = x._2.asInstanceOf[List[Map[String,Any]]]
        elementList foreach (item => {
          list += (buf.append(serialize(item.asInstanceOf[Map[String,Any]])).toString)
        })
        list(list.size - 1) += "]"
      }
      else {
        buf.append(f(x._2))
        list += buf.toString
      }
    })

    s"{\n${list.mkString(",\n")}}"
  }

  def f[T](v: T) = v match {
    case _: String => "\"" + v + "\""
    case _         => v
  }

}

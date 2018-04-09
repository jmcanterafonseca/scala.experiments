package fiware

import java.io.File
import scala.collection.mutable
import scala.io.Source

/**
  *
  *   DMO Reader for CMM Machines .- Industrial Data Space
  *
  *   Copyright (c) 2018 FIWARE Foundation e.V.
  *
  *   License: MIT
  *
  */
object DMOReader {
  val datePattern = raw"DATE=(\d{4})/(\d{2})/(\d{2})".r
  val timePattern = raw"TIME=(.*)".r
  val filePattern = raw"FILNAM/'(.+)'".r

  // FA(Esfera_10)=FEAT/SPHERE,CART,74.8580,58.0652,3.7478,27.8511
  val faPattern = raw"FA\((.+)\)=FEAT/(.+),(.+)".r
  val taPattern = raw"TA\((.+)\)=TOL/CORTOL,(.+),([\+\-\\.\d]+),([A-Z]+)".r

  val PrepAt = Vocabulary.PreparedAt

  def main(args: Array[String]): Unit = {
    val fileName = args(0)
    val file = new File("./src/test/resources" + File.separator + fileName)

    dmo_file_proccess(Source.fromFile(file.getCanonicalPath))
  }

  def dmo_file_proccess(source: Source) = {
    // Map with the data processed

    val data = new mutable.HashMap[String, Any]()
    f_property(data, "type", Vocabulary.EntityType)
    f_property(data,Vocabulary.Device,
      f_ngsi_value("Trimek CMM Spark Gauge Plus"))
    f_property(data,Vocabulary.Org,f_ngsi_value("Trimek"))
    f_property(data,Vocabulary.Prep,f_ngsi_value("Innovalia"))

    val linesCat = prepare_file(source)
    println(linesCat.size)

    for (line <- linesCat) {
      f_match(line, data)
    }

    // data.map((pair) => (pair._1, f_ser_json(pair._2)))

    println(data("preparedAt"))
    println(data)
  }

  // prepares the DMO file by merging lines so that we can process in an easier way
  def prepare_file(source:Source):mutable.ArrayBuffer[String] = {
    var linesCat = new mutable.ArrayBuffer[String]
    var concatLine:String = null

    for(line <- source.getLines) {
      if (line endsWith("$")) {
        concatLine = line
      }
      else {
        if (concatLine != null) {
          linesCat += (concatLine.take(concatLine.length - 1) + line)
          concatLine = null
        }
        else linesCat += line
      }
    }

    linesCat
  }

  /**
    *
    *  Parses each line by applying pattern matching
    *
    * @param str
    * @param map
    *
    *
    */
  def f_match(str:String, map:mutable.HashMap[String,Any]) =  str match {
    // Date and Time
    case datePattern(year,month,day) => map += (PrepAt -> s"${year}-${month}-${day}")
    case timePattern(timeStr) => map.update(PrepAt, f_ngsi_value(map(PrepAt) + s"T${timeStr}", "DateTime"))

    // File processed and automatically generated Id
    case filePattern(fileName) => map += ("fileName" -> f_ngsi_value(s"${fileName}"))

    // TAs
    case taPattern(featureName,featureType,deviation,tolerance) => {
      val meta = Map("tolerance" -> f_ngsi_value(tolerance))
      map += (s"TA:${featureName}" -> f_ngsi_value(deviation.toFloat,featureType,metadata = meta))
    }

    // FAs
    case faPattern(featureName, featureType, faValue) => println(s"${featureName}, ${featureType}, ${faValue}")

    case _ => Nil
  }

  // Generates an NGSI value
  def f_ngsi_value(value:Any, t_type:String=null, metadata:Map[String,Any]=null):Map[String,Any] = {
    var map = Map("value" -> value)
    if (t_type != null) {
      map += ("type" -> t_type)
    }

    if (metadata != null) {
      map + ("metadata" -> metadata)
    }
    else map
  }

  def f_property(map:mutable.HashMap[String, Any],name:String,value:Any) = {
    map +=(name -> value)
  }


/*
  def f_ser_json[T](v: T) = v match {
    case _: String => s"${_}"
    case _: Any    => v
  } */
}

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
  val faPattern = raw"FA\((.+)\)=FEAT/([A-Z,]+)([\+\-\\.\d,]+)".r
  val taPattern = raw"TA\((.+)\)=TOL/CORTOL,(.+),([\+\-\\.\d]+),([A-Z]+)".r

  val PrepAt = Vocabulary.PreparedAt

  def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def main(args: Array[String]): Unit = {
    if(args.size <3) {
      println("Usage: DMOReader <directory> <endpoint> <tenant>")
      System.exit(-1)
    }

    val directory = args(0)
    val endpoint = args(1)
    val tenant = args(2)
    val files = getListOfFiles(directory)

    files foreach (file => {
      println(s"Reading: ${file}")
      val data = dmo_file_process(Source.fromFile(file.getCanonicalPath))

      println(s"Writing to ${endpoint}")
      NgsiWriter.write(endpoint, data.toMap[String,Any], tenant)
    })
  }

  def dmo_file_process(source: Source):mutable.HashMap[String,Any] = {
    // Map with the data processed
    val data = new mutable.HashMap[String, Any]()

    // f_property(data, "id", randomUUID().toString)
    f_property(data, "type", Vocabulary.EntityType)

    val linesCat = prepare_file(source)
    println(linesCat.size)

    for (line <- linesCat) {
      f_match(line, data)
    }

    data
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
    case filePattern(fileName) => {
      map += ("id" -> s"${fileName.replace(' ','_')}")
      map += ("fileName" -> f_ngsi_value(s"${fileName}"))
    }

    // TAs
    case taPattern(featureName,featureType,deviation,tolerance) => {
      val meta = Map("tolerance" -> f_ngsi_value(tolerance))
      map += (s"TA:${sanitize(featureName)}" -> f_ngsi_value(deviation.toFloat,featureType,metadata = meta))
    }

    // FAs
    case faPattern(featureName, featureType, faValue) => {
      val valueList = faValue.split(",").map(x => x.toFloat).toList
      map += (s"FA:${sanitize(featureName)}" -> f_ngsi_value(valueList,featureType.take(featureType.length - 1)))
      println(s"${featureType}")
    }

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

  def sanitize(s:String):String = {
    s.map(c => c match {
        case 'á' => 'a'
        case 'é' => 'e'
        case 'í' => 'i'
        case 'ó' => 'o'
        case 'ú' => 'u'
        case _ => c
      }
    )
  }
}

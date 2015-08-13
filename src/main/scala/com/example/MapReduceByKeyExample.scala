package com.example

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import play.api.libs.json.Json
import play.api.libs.json.JsNull
import org.joda.time.DateTime
import org.apache.spark.rdd.RDD


//import the implicit conversions - e.g. for reduceByKey
import org.apache.spark.SparkContext._

object MapReduceByKeyExample {
  class Record(val name: String, val org_reference: String, val created_at: DateTime, val updated_at: DateTime, val original_meta: String, val other: String) extends java.io.Serializable {
    def key() = {
      name + "_" + org_reference + "_" + created_at
    }
    override def toString() = {
      name + ";" + org_reference + ";" + created_at + ";" + updated_at + ";" + original_meta + ";" + other
    }
  }

  def main(args: Array[String]): Unit = {
    println("start..")

    val conf = new SparkConf().setAppName("test1").setMaster("local")
    val sc = new SparkContext(conf)

    val lines = sc.textFile("data.txt")

    val records = lines.map { x =>
      {
        val data = x.split(";").map(_.trim)
        val updatedAt = (Json.parse(data(3)) \ "updated_at").toString().replace("\"", "").trim()
        new Record(data(0), data(1), DateTime.parse(data(2)), DateTime.parse(updatedAt), data(3), data(4))
      }
    }
    val ala = records.map(record => (record.key() -> record))
//    ala.reduceByKey(
//        (r1, r2) =>
//          {
//            if (r1.updated_at.isBefore(r2.updated_at))
//              r2
//            else
//              r1
//          })
//      .foreach { x => println(" data: " + x._1 + " -- " + x._2) }
//
//    println("stopping spark...")
    sc.stop()
    println("finished")

  }
}
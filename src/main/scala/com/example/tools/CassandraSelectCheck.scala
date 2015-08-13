package com.example.tools

import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.SparkConf

/**
 * Example how to directly run Cassandra query
 */
object CassandraSelectCheck {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", "127.0.0.1")
    //.set("spark.cassandra.auth.username", "cassandra")            
    //.set("spark.cassandra.auth.password", "cassandra")

    val result = CassandraConnector(conf).withSessionDo { session =>
      session.execute("select * from test_migration_1.test_from;")
    }
    while (result.iterator().hasNext()) {
      val row = result.iterator().next()
      printf(" row: %s - %s \n", row.getUUID("main_key"), row.getString("name"))
    }

  }
}
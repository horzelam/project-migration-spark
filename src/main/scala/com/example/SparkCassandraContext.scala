package com.example

import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.rdd.CassandraRDD
import com.datastax.spark.connector.CassandraRow
import org.apache.spark.SparkContext

class SparkCassandraContext(val connector: CassandraConnector,
                       val rdd: CassandraRDD[CassandraRow],
                       val sparkContext: SparkContext)
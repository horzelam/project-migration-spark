package com.example

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext.rddToPairRDDFunctions
import org.joda.time.DateTime
import com.datastax.driver.core.ResultSet
import com.datastax.spark.connector.CassandraRow
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.rdd.CassandraRDD
import com.datastax.spark.connector.toSparkContextFunctions
import com.example.model.EntityCreator

object SchemaSimpleMigration {
  val sourceKeyspace: String = "test_migration_1"
  val sourceTableName: String = "entities"

  def main(args: Array[String]): Unit = {
    printf(s"\nStarting SchemaSimpleMigration ...")
    val context = connectToCassandra()
    val connector = context.connector;

    // shops to migrate
    val shops = Array(
        "f8de4f78-d9a5-491c-b6d9-4a3066630c9d" , "7d328fa2-f422-4408-b62c-6d18437344d3", "085df4a9-f718-48a8-a6ba-710305ddd451"
    )
    
    migrateAll(context.rdd, connector, shops)
    context.sparkContext.stop()
  }

  def connectToCassandra(): SparkCassandraContext = {
    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", "10.0.106.42") // CASSANDRA CONTACT POINT
      //.setMaster("local") // SPARK MASTER
      .setMaster("10.0.106.80") // SPARK MASTER
      .setAppName("Simple HiSpark Application")
      
      // prod spark: 10.0.106.80 / 81 
      
    val sc = new SparkContext(conf)
    //val sc = new SparkContext("spark://127.0.0.1:7077", "test", conf)
    //?? "spark://master:7077"
    val connector = CassandraConnector(conf)
    val rdd = sc.cassandraTable(sourceKeyspace, sourceTableName)
    new SparkCassandraContext(connector, rdd, sc)
  }

  def checkShouldOverwrite(newSchemaRecordIterator: java.util.Iterator[com.datastax.driver.core.Row], updatedAt: Option[DateTime]): Boolean = {
    if (newSchemaRecordIterator.hasNext()) {
      updatedAt match {
        case Some(migratedUpdatedAt) => {
          val existingUpdatedAtRow = newSchemaRecordIterator.next();
          if (!existingUpdatedAtRow.isNull("updated_at")) {
            return existingUpdatedAtRow.getDate("updated_at").before(migratedUpdatedAt.toDate())
          } else {
            true
          }
        }
        case None => false
      }
    } else {
      true
    }
  }

  def migrateAll(rdd: CassandraRDD[CassandraRow], connector: CassandraConnector, shops: Array[String]) = {
    // 942a4841-a20f-4eb3-b9dd-b56e9cc52a13 - MAGENTO
    // f8de4f78-d9a5-491c-b6d9-4a3066630c9d - EBAY small connection - 4 entities in new schema , 36 entities in old
    // 7d328fa2-f422-4408-b62c-6d18437344d3- EBAY small connection - 12 entities in new schema 
    // 085df4a9-f718-48a8-a6ba-710305ddd451- EBAY small connection - 6 entities in new schema 
    // 8d206341-ea14-4026-8191-33818b86bd7e - 1 000 entities entities in new schema
    // 69fb0474-a935-4467-bd19-d1f99aa8713e - 10 000 entities entities in new schema
    // 171e145e-64b1-4e6b-b8d2-00963ee83a35 - 100 000 entities (biggest one) entities in new schema
    val startedDate = DateTime.now()

    // make it paraller but on driver side only (not on Spark worker)
    shops.par.map(shopId => {

      // run migration
      val connectionStats = migrateSingleShop(rdd, connector, shopId)

      // group the results:      
      val collected = connectionStats.coalesce(1, true)

      // get the stats and print the output reports
      val totalConnStats = collected.reduce((s1, s2) => new Stats("", s1.exists + s2.exists, s1.written + s2.written))
      collected.map(_.msg).saveAsTextFile("output/" + shopId + "/" + startedDate.getHourOfDay + "_" + startedDate.getMinuteOfHour)

      //records = results.count();
      " FINISHED " + shopId + " - exists: " + totalConnStats.exists + ", written: " + totalConnStats.written
    }).foreach(println(_))
    println(" time : " + (DateTime.now().getMillis - startedDate.getMillis) + " ms")

    //.collect() //just to run everything, since nothing exectuted till this moment (RDDs are lazy distributed collections)

  }

  def migrateSingleShop(rdd: CassandraRDD[CassandraRow], connector: CassandraConnector, shopId: String) = {
    val newSchemaRepo = new NewSchemaRepository(connector);
    val finalWritesRDD = rdd.where("connection_id = " + shopId) //  and entity == 'transaction'    
     // .limit(100)
      //.filter(row => !row.getString("platform").equals("magento"))
      .filter(row => row.getString("platform").equals("ebay")) // additional check - ONLY ebay data
      .map(row => EntityCreator.createEntity(row))
      .map(entity => (entity.primaryKey() -> entity))
      .reduceByKey((entity1, entity2) => {
        if (entity1.isNewer(entity2))
          entity1
        else
          entity2
      })      
      .map {
        // trigger WRITE to TARGET storage
        case (key, entity) => {
          //println("-- grouped migrated entity for : " + entity.primaryKey())
          println("- writing : " + entity.entity + " # " + key)
          (key -> newSchemaRepo.write(entity))
        }
      }

    // wait for WRITE to finish writing to TARGET storage
    finalWritesRDD.map(x => {
      val rs = x._2.getUninterruptibly()
      generateResultSetInfo(rs, x._1)
    })

  }

  class Stats(val msg: String, val exists: Long, val written: Long) extends Serializable

  def generateResultSetInfo(rs: ResultSet, key: String): Stats = {
    val row = rs.one()
    if (!row.getBool("[applied]")) {
      //      val entity = extract_entity(row.getString("entity_name"));
      new Stats("TARGET exists : " + key, 1, 0)
    } else {
      new Stats("TARGET written: " + key, 0, 1)

    }
  }

  def extractEntityTypeFromName(entityName: String) = {
    val lastIndx = entityName.lastIndexOf("_");
    entityName.substring(lastIndx + 1)
  }

}
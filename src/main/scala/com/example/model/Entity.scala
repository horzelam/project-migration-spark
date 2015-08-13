package com.example.model

import java.util.UUID
import org.joda.time.DateTime
import com.datastax.spark.connector.CassandraRow
import com.example.tools.JsonExtractor

/**
 * Represents migrated entity
 */
trait Entity {

  def updatedAt: Option[DateTime]

  // type of entity: profile/transaction/buyer/product
  def entity: String

  /**
   * PrimaryKey represents the TARGET table key.<BR>
   * We expect multiple records in SOURCE table with the same PrimaryKey- that's why grouping is done during migration.
   */
  def primaryKey(): String

  /**
   * Return TRUE if current updateAt is set and its newer than other's updatedAt
   */
  def isNewer(other: Entity): Boolean = {
    updatedAt match {
      case Some(thisDate) => other.updatedAt match {
        case Some(otherDate) => thisDate.isAfter(otherDate)
        case None            => true
      }
      case None => false
    }
  }

  override def toString() = {
    primaryKey() + "-" + updatedAt
  }
}

object EntityCreator {

  val extractor = new JsonExtractor()

  def createEntity(row: CassandraRow): Entity = {
    val connection_id = row.getUUID("connection_id")
    val entity = row.getString("entity")
    val version = row.getString("version")

    val createdAt = row.getDateTime("created_at")

    val platform = row.getString("platform")
    val routing_key = row.getString("routing_key")
    val entity_name = row.getString("entity_name")
    val userId = row.getUUID("user_id")
    val original_meta = row.getString("original_meta")
    val payload = row.getString("payload")

    // not used in target schema:
    val operation = row.getString("operation")
    val id = row.getString("id")

    val createdAtWeek = createdAt.getYear() + "$" + createdAt.getWeekOfWeekyear() //extract from created_at
    val originalReference = extractOriginalReference(payload, platform, entity) //extract from payload
    val updatedAt = extractUpdatedAt(original_meta) //extract from meta data

    val result = originalReference match {
      case Some(orgReferenceValue) =>
        {
          entity match {
            case "profile" =>
              new ProfileEntity(connection_id, entity, version, orgReferenceValue, createdAt, updatedAt, platform, routing_key, entity_name, userId, original_meta, payload)

            case _ =>
              new GenericEntity(connection_id, entity, createdAtWeek, version, orgReferenceValue, createdAt, updatedAt, platform, routing_key, entity_name, userId, original_meta, payload)
          }

        }
      case None => throw new RuntimeException("Unable to convert the row: " + platform + "," + connection_id + "," + entity + "," + createdAt + "," + version + "," + operation + "," + id)
    }
    println("- SOURCE: " + result.entity + " # " + result.primaryKey())
    result
  }

  def extractOriginalReference(payload: String, platform: String, entity: String): Option[String] = {
    extractor.extractOriginalReference(payload, platform, entity)
  }

  def extractUpdatedAt(original_meta: String): Option[DateTime] = {
    extractor.extractUpdatedAt(original_meta).map { date => DateTime.parse(date) }
  }

}
class ProfileEntity(val connection_id: UUID, val entity: String, val version: String, val originalReference: String, val createdAt: DateTime, val updatedAt: Option[DateTime],
                    val platform: String, val routingKey: String, val entityName: String, val userId: UUID, val original_meta: String, val payload: String) extends Entity with java.io.Serializable {

  /**
   * Returns key which is representing PRIMARY KEY in TARGET table
   */
  override def primaryKey(): String = {
    connection_id + "#" + createdAt.toString() + "#" + version + "#" + originalReference;
  }

}

class GenericEntity(val connection_id: UUID, val entity: String, val createdAtWeek: String, val version: String, val originalReference: String, val createdAt: DateTime, val updatedAt: Option[DateTime],
                    val platform: String, val routingKey: String, val entityName: String, val userId: UUID, val original_meta: String, val payload: String) extends Entity with java.io.Serializable {

  /**
   * Returns key which is representing PRIMARY KEY in TARGET table
   */
  override def primaryKey(): String = {
    connection_id + "#" + entity + "#" + createdAtWeek + "#" + createdAt.toString() + "#" + version + "#" + originalReference;
  }

}

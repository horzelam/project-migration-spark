package com.example

import org.joda.time.DateTime
import com.datastax.spark.connector.cql.CassandraConnector
import com.example._
import com.example.model._

/**
 * Repository to access TARGET table
 */
class NewSchemaRepository(val connector: CassandraConnector) extends Serializable {

  val TARGET_ENTITIES_PROFILE_TABLE = "test_migration_1.entities_profile"
  val TARGET_ENTITIES_GENERIC_TABLE = "test_migration_1.entities_generic"

  // the Cassandra prpeared statements 
  def writeProfileStmt(connector: CassandraConnector) =
    connector.withSessionDo { session =>
      session.prepare(s"INSERT INTO ${TARGET_ENTITIES_PROFILE_TABLE} (connection_id,version,original_reference,created_at,updated_at,platform,routing_key,entity_name,user_id,original_meta,payload) " +
        "values (?,?,?,?,?,?,?,?,?,?,?) IF NOT EXISTS")
    }
  def writeGenericStmt(connector: CassandraConnector) =
    connector.withSessionDo { session =>
      session.prepare(s"INSERT INTO ${TARGET_ENTITIES_GENERIC_TABLE} (connection_id,entity,created_at_week,version,original_reference,created_at,updated_at,platform,routing_key,entity_name,user_id,original_meta,payload) " +
        "values (?,?,?,?,?,?,?,?,?,?,?,?,?) IF NOT EXISTS")
    }

  def write(migratedEntity: Entity) = {
    connector.withSessionDo(session => {
      val start = System.currentTimeMillis()
      val bound = migratedEntity.entity match {
        case "profile" => {
          val profile: ProfileEntity = migratedEntity.asInstanceOf[ProfileEntity]

          writeProfileStmt(connector).bind(profile.connection_id, profile.version, profile.originalReference, profile.createdAt.toDate(), profile.updatedAt.map(_.toDate()).getOrElse(null), profile.platform, profile.routingKey, profile.entityName, profile.userId, profile.original_meta, profile.payload)

        }
        case _ => {
          val generic: GenericEntity = migratedEntity.asInstanceOf[GenericEntity]
          writeGenericStmt(connector).bind(generic.connection_id, generic.entity, generic.createdAtWeek, generic.version, generic.originalReference, generic.createdAt.toDate(), generic.updatedAt.map(_.toDate()).getOrElse(null), generic.platform, generic.routingKey, generic.entityName, generic.userId, generic.original_meta, generic.payload)
        }
      }
      val time = System.currentTimeMillis() - start
      session.executeAsync(bound)
    })
  }

}
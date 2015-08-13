package com.example

class ExistingEntityFilter {
  // FILTERING ALREADY EXISTING RECORDS - explicit check
  //      .filter {        
  //        case (key, entity) => {
  //          val start = System.currentTimeMillis()
  //          val notInNewSchema = newSchemaRepo.isNotInNewSchema(entity)
  //          val time = System.currentTimeMillis() - start
  //          //println("--- time to check new schema: " + time)
  //          if (!notInNewSchema) {
  //            println("--- in new schema : " + entity.entity + " # " + key )
  //          }
  //          notInNewSchema
  //        }
  //      }
  // we could also consider preparing RDD with existing Keys and then use : rdd.subtractByKey
}
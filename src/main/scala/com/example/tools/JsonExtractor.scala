package com.example.tools

import play.api.libs.json._

class JsonExtractor {

  /**
   * Extracts Updated at from Meta.
   */
  def extractUpdatedAt(meta: String): Option[String] = {
    val parsed = Json.parse(meta)
    try {
      
      val result = (parsed \ ("raw_entity") \ ("updated_at") ).as[String];
      Some(result)
    } catch {
      case e: RuntimeException => None
    }
  }

  def extractDefaultId(payload: String, nodeNames:String*) = {
    try {
      val result = nodeNames.foldLeft(Json.parse(payload))((n1, n2 ) => n1 \ n2).as[String]
      
      //val result = Json.parse(payload) \ mainNode \ "id"
      Some(result)
    } catch {
      case e: RuntimeException => None
    }
  }
  
   

  /**
   * Extracts original reference from payload.
   * See: https://docs.google.com/spreadsheets/d/1_rf9Tp7ds_GK8-qNxFftVlotaDMYtjKMkmHMSVuSrHU/edit#gid=0
   */
  def extractOriginalReference(payload: String, platform: String, entity: String): Option[String] = {
    entity match {
      case "transaction" => {
        platform match {
          case "ebay" => extractDefaultId(payload,"transaction","OrderID")
              //            {
              //            val pattern = """OrderID":[ ]*"([^"]*)"""".r.unanchored
              //            val allmatching = (pattern findAllMatchIn payload).map(x => x.group(1)).toList
              //            allmatching.size match {
              //              case 1 => Some(allmatching(0))
              //              case _ => None
              //            }
              //            //           payload match {
              //            //              case pattern(orderId) => orderId
              //            //           }
              //          } //transaction.OrderID
          case "bigcommerce" => extractDefaultId(payload,"transaction","id")
          case "seoshop"     => extractDefaultId(payload,"transaction","id")
          case "shopify"     => extractDefaultId(payload,"transaction","id")
          case "magento"     => None
          case _             => None
        }
      }

      case "product" => {
        platform match {
          case "ebay"        => extractDefaultId(payload,"product","ItemID")
          case "bigcommerce" => extractDefaultId(payload,"product","id")
          case "seoshop"     => extractDefaultId(payload,"product","id")
          case "shopify"     => extractDefaultId(payload,"product","id")
          case "magento"     => None
          case _             => None
        }
      }

      case "profile" => {
        platform match {
          case "ebay"        => extractDefaultId(payload,"profile","EIASToken")
          case "bigcommerce" => extractDefaultId(payload,"profile","id")
          case "seoshop"     => extractDefaultId(payload,"profile","id")
          case "shopify"     => extractDefaultId(payload,"profile","id")
          case "magento"     => None
          case _             => None
        }
      }

      case "buyer" => {
        platform match {
          case "ebay"        => extractDefaultId(payload,"buyer","EIASToken")
          case "bigcommerce" => extractDefaultId(payload,"buyer","id")
          case "seoshop"     => extractDefaultId(payload,"buyer","id")
          case "shopify"     => extractDefaultId(payload,"buyer","id")
          case "magento"     => None
          case _             => None
        }
      }
    }
  }

}
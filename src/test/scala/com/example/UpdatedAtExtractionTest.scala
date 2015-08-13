
import org.scalatest._
import com.example.tools.JsonExtractor

/**
 * To be removed.
 * Just left it to show different approach for testing.
 */
class UpdatedAtExtractionTest extends FlatSpec with Matchers {

  val meta = """{ 
    "event_id":"351d276b-82d2-48a2-8ac0-70e7ac69d5b4",
    "event_name":"transaction.create",
    "raw_entity":{
      "name":"magento_raw_transaction",
      "version":"1.0",
      "created_at":"2015-07-07T18:08:21.000+0000",
      "updated_at":"2015-07-07T18:08:24.000+0000",
      "original_reference":"335"},
      "origin":{
        "platform":"magento",
        "connection_uuid":"942a4841-a20f-4eb3-b9dd-b56e9cc52a13",
        "user_id":"9d7288d6-1f5a-47a3-9eb2-02a66e3f1a59"
      },
    "created_at":"2015-07-15T09:10:25.000+0000"
    }"""

  val extractor = new JsonExtractor();

  "MetaJsonExtractor extracting updated_at " should " return proper value" in {
    extractor.extractUpdatedAt(meta).get === "2015-07-07T18:08:24+0000"
  }

}

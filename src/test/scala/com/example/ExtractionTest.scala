import org.scalatest._
import com.example.tools.JsonExtractor

class ExtractionTest extends FeatureSpec with GivenWhenThen {

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

  info("As a migration script")
  info("I want to be able to extract UpdatedAt data")
  info("So I can migrate the data using that field")

  val extractor = new JsonExtractor();

  feature("UpdatedAt extraction") {

    scenario("UpdatedAt extraction is run when updatedAt is present in meta") {
      Given("a Meta with updated_at field set to some value")
      When("the UpdatetAt is extracted")
      val result = extractor.extractUpdatedAt(meta)
      Then("the result should be the proper date")
      assert(result.get === "2015-07-07T18:08:24.000+0000")
    }

    scenario("UpdatedAt extraction is run when updatedAt is not present in meta") {
      Given("any JSON data")
      When("the UpdatetAt is extracted")
      val result = extractor.extractUpdatedAt("""{}""")
      Then("the result should be the None")
      assert(result === None)
    }

  }

  feature("OriginalReference extraction") {

    scenario("originalReference extraction is run on EBAY Tx when field is present inside transaction") {
      Given("an ebay Tx Payload with original_reference field set to some value")
      When("the value is extracted")
      val result = extractor.extractOriginalReference("""{
                "transaction": 
                {
                    "OrderID":"181757115135-1377749923008",
                    "OrderStatus": "Completed",
                    "AdjustmentAmount": "0.0",
                    "AmountPaid": "1.99",
                    "AmountSaved": "0.0"
                }
              }""", "ebay", "transaction")
      Then("the result should be the proper value")
      assert(result.get === "181757115135-1377749923008")
    }

    scenario("originalReference extraction is run on EBAY Tx when field is present inside transaction and outside it") {
      Given("an ebay Tx Payload with original_reference field set in multiple places")
      When("the value is extracted")
      val result = extractor.extractOriginalReference("""{
              "transaction": 
              {
                  "OrderID": "181757115135-1377749923008",                  
                  "OrderStatus": "Completed",
                  "AdjustmentAmount": "0.0",
                  "AmountPaid": "1.99",
                  "AmountSaved": "0.0"
              },
              "other":
              {
                "OrderID": "181757115135-1377749923008"
              }
            }""", "ebay", "transaction")
      Then("the result should  be the proper value")
      assert(result.get === "181757115135-1377749923008")
    }

    scenario("originalReference extraction is run on SHOPIFY Tx when field is present inside transaction") {
      Given("an ebay Tx Payload with original_reference field set in multiple places")
      When("the value is extracted")
      val result = extractor.extractOriginalReference("""{
              "other":
              {
                "id": "181757115135-1377749923008"
              },
              "transaction": 
              {
                  "id": "12312123-12312312312312",                  
                  "OrderStatus": "Completed",
                  "AdjustmentAmount": "0.0",
                  "AmountPaid": "1.99",
                  "AmountSaved": "0.0"
              },
              "other":
              {
                "OrderID": "181757115135-1377749923008"
              }
            }""", "shopify", "transaction")
      Then("the result should be None")
      assert(result.get === "12312123-12312312312312")
    }
  }

}

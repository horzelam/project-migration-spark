
import com.example.model._
import collection.mutable.Stack
import org.scalatest._
import org.joda.time.DateTime
import java.util.UUID

class EntityTest extends FlatSpec with Matchers {

  "A method Entity.isNever() " should " return proper results for particular entities" in {

    val tx1 = new GenericEntity(UUID.randomUUID(), "transaction", "2015$12", "1.0", "123123-123123", DateTime.now(), Some(DateTime.now()), "ebay", "some-routing-key", "transaction_raw_ebay", UUID.randomUUID(), "", "")
    val tx2 = new GenericEntity(UUID.randomUUID(), "transaction", "2015$12", "1.0", "123123-123123", DateTime.now(), Some(DateTime.now().plusYears(1)), "ebay", "some-routing-key", "transaction_raw_ebay", UUID.randomUUID(), "", "")
    val txNoUpdatedDate = new GenericEntity(UUID.randomUUID(), "transaction", "2015$12", "1.0", "123123-123123", DateTime.now(), None, "ebay", "some-routing-key", "transaction_raw_ebay", UUID.randomUUID(), "", "")

    tx2.isNewer(tx1) should be(true)
    tx2.isNewer(txNoUpdatedDate) should be(true)
    tx1.isNewer(txNoUpdatedDate) should be(true)
    
    txNoUpdatedDate.isNewer(tx1) should be(false)

  }

}
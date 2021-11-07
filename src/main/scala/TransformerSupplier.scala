import java.util
import java.util.Collections
import io.github.azhur.kafkaserdecirce.CirceSupport
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.{Transformer, TransformerSupplier}
import org.apache.kafka.streams.processor.{ProcessorContext, PunctuationType}
import org.apache.kafka.streams.scala.Serdes
import org.apache.kafka.streams.state.{KeyValueIterator, KeyValueStore, StoreBuilder, Stores}
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.decode

// Case class for the input stream "events"
case class EventVal(timestamp: Long, city_id: Long, city_name: String, refugees: Long)
// Case class for the output stream "updates"
case class UpdateVal(city_id: Long, city_name: String, refugees: Long)

// Configuring the transformerSupplier to be able to create the "Transformer" instance that is needed for the application
class CustomTransformerSupplier (storeName: String) extends TransformerSupplier[String,String,KeyValue[String,String]] with CirceSupport{

  // Editing the "get()" method of the "TransformerSupplier" class to match the required transformer
  override def get(): Transformer[String,String,KeyValue[String,String]] = {

    // Initializing a Transformer instance that has Strings as key value pairs for both input and output
    new Transformer[String,String,KeyValue[String,String]] {
      // "context" remains unaltered
      var context: ProcessorContext = _
      // keyValueStore remains unaltered
      var keyValueStore: KeyValueStore[String, Long] = _
      //
      override def init(context: ProcessorContext): Unit = {
        this.keyValueStore = context.getStateStore(storeName)
        this.context=context
      }

      override def transform(key: String, value: String): KeyValue[String,String] = {


        implicit val eventValues: Array[String] = decode[EventVal](value).getOrElse().toString.split(",")
        val city_id = eventValues(1)
        val city_name = eventValues(2)
        val newNbrRefugees = eventValues(3).replace(")", "").toLong

        //Fetch the latest currentTotalRefugees. If currentTotalRefugees is zero, initialize it to zero.
        var currentTotalRefugees: Long = keyValueStore.get(city_id)
        if (currentTotalRefugees == 0) {
          currentTotalRefugees = 0
        }

        val newTotal: Long = currentTotalRefugees + newNbrRefugees
        keyValueStore.put(city_id, newTotal)


        KeyValue.pair(city_id.toLong.asJson.toString(),
                     UpdateVal(city_id.toLong, city_name, newTotal).asJson.toString()
                                                                    .replaceAll("\n", "")
                                                                    .replaceAll(" ", ""))
      }

      override def close(): Unit = {}
    }
  }

  // Editing the "stores()" method that will be used in the statefull "state-store" operation to keep track of total
  // number of refugees for each city.
  override def stores(): (util.Set[StoreBuilder[_]]) = {

    val storeName : String = "totalRefugees"
    // The state-store will have names of cities as key, and the total number of refugees as a Long as value
    val keyValueStoreBuilder: StoreBuilder[KeyValueStore[String,Long]]  = {
      // Using generic serdes to serialize/deserialize the state-store key and value
      Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(storeName), Serdes.String, Serdes.Long)
    }
    Collections.singleton(keyValueStoreBuilder)
  }

}

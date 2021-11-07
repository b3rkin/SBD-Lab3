import java.time.Duration
import java.util.Properties
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.apache.kafka.streams.scala.ImplicitConversions._

object Transformer extends App{
  import Serdes._

  // Setting the properties of the Kafka client that will be used for the transformer
  val props: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "transformer")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-server:9092")
    p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer.getClass.getName)
    p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Integer.getClass.getName)
    p
  }

  // Initializing a StreamsBuilder instance
  val builder: StreamsBuilder = new StreamsBuilder
  // Use the StreamsBuilder instance to initialize a KStream from the input topic "events"
  val events: KStream[String, String] = builder.stream[String, String]("events")

  // Specifying the name of the state-store that will be used to keep track of total number of refugees in each city
  val storeName : String = "totalRefugees"

  // Applying the transformer that is specified in the "CustomTransformerSupplier" class to the KStream that is
  // formed of the input stream from the "events" class and form a new KStream "output"
  val output: KStream[String,String] = events.transform(new CustomTransformerSupplier(storeName))

  // Publish the "output" KStream to the "updates" topic.
  output.to("updates")

  // Initializing the Kafka client that will process the input stream, with previously set configuration
  val streams: KafkaStreams = new KafkaStreams(builder.build(), props)

  // Cleaning up the local state store directory by deleting all data that might reside from previous runs
  streams.cleanUp()
  // Start the KafkaStreams instance by starting all its threads
  streams.start()

  // Shutdown the "streams" KafkaStreams instance by signaling all the threads to stop
  sys.ShutdownHookThread {
    val timeout = 10;
    streams.close(Duration.ofSeconds(timeout))
  }
}

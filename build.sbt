name := "Transformer"
version := "1.0"
scalaVersion := "2.13.6"

scalastyleFailOnWarning := true

run / fork := true
run / connectInput := true
outputStrategy := Some(StdoutOutput)

val circeVersion = "0.14.1"

libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-simple" % "1.8.0-beta4",
    "org.apache.kafka" %% "kafka-streams-scala" % "3.0.0",
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "io.github.azhur" %% "kafka-serde-circe" % "0.6.3",
    "com.sksamuel.avro4s" %% "avro4s-core" % "4.0.11"
)
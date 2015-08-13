import AssemblyKeys._

////////////////////////////////////////////////////////////////////////
// Based on https://github.com/rssvihla/spark_commons
////////////////////////////////////////////////////////////////////////

name := "project-migration-spark"
organization := "Example"
scalaVersion := "2.10.5"
version := "1.0.0"
parallelExecution in Test := false
fork in Test := true

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

val Spark = "1.2.1"

val spark_core = "org.apache.spark" % "spark-core_2.10" % Spark % "provided"
val spark_sql = "org.apache.spark" % "spark-sql_2.10" % Spark % "provided"

val spark_connector = ("com.datastax.spark" %% "spark-cassandra-connector" % Spark withSources() withJavadoc()).
  exclude("com.esotericsoftware.minlog", "minlog").
  exclude("commons-beanutils","commons-beanutils")

val spark_connector_java = ("com.datastax.spark" %% "spark-cassandra-connector-java" % Spark withSources() withJavadoc()).
  exclude("org.apache.spark","spark-core")
  
val sparkDependencies = Seq(spark_core,
  spark_sql,
  spark_connector,
  spark_connector_java)
  
val testDependencies = Seq(
  "org.scalatest" % "scalatest_2.10" % "2.2.4" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "com.github.javafaker" % "javafaker" % "0.5")


otherDependencies = Seq(
	"com.typesafe.play" %% "play-json" % "2.2.1",
	"org.apache.commons" % "commons-math3" % "3.4.1",
	"net.jpountz.lz4" % "lz4" % "1.2.0",
	// for json parsing:
	"com.typesafe.play.extras" %% "iteratees-extras" % "1.5.0"
)
libraryDependencies ++= sparkDependencies
libraryDependencies ++= testDependencies
libraryDependencies ++= otherDependencies



//We do this so that Spark Dependencies will not be bundled with our fat jar but will still be included on the classpath
//When we do a sbt/run
run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))

assemblySettings

jarName in assembly := name.value + "-assembly.jar"

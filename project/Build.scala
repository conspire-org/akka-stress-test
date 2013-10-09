import sbt._
import sbt.Keys._
import sbt.Tests._
import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{ Dist, outputDirectory, distJvmOptions, configSourceDirs, distMainClass, additionalLibs }

object AkkaStressTestBuild extends Build {
  
  System.setProperty("java.awt.headless", "true") // Prevent SBT stealing focus on OS X

  val appName = "akka-stress-test"
  
  val appVersion = "0.1-SNAPSHOT"

  object V {
    val Scala = "2.10.2"
    val Akka = "2.2.1"
  }

  val akkaDependencies = Seq(
    "com.typesafe.akka" %% "akka-actor" % V.Akka,
    "com.typesafe.akka" %% "akka-slf4j" % V.Akka,
    "com.typesafe.akka" %% "akka-kernel" % V.Akka,
    "com.typesafe.akka" %% "akka-cluster" % V.Akka,
    "com.typesafe.akka" %% "akka-camel" % V.Akka,
    "com.typesafe.akka" %% "akka-contrib" % V.Akka,
    "com.typesafe.akka" %% "akka-testkit" % V.Akka
  )
 
  lazy val versionReport = TaskKey[String]("version-report")

  val nodeSettings = Project.defaultSettings ++ Seq(
    version := appVersion,
    scalaVersion := V.Scala,
    exportJars := true,
    parallelExecution in Test := false,
    resolvers ++= Seq(
      "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      "Objectstyle repository" at " http://objectstyle.org/maven2/"
    ),
    libraryDependencies ++= akkaDependencies ++ Seq(
      "com.typesafe" % "config" % "1.0.0",
      "ch.qos.logback" % "logback-classic" % "1.0.7",
      "com.typesafe.play" % "play-json_2.10" % "2.2.0"
    )
  )
  
  lazy val node = Project(
    id = "test-node",
    base = file("."),
    settings = nodeSettings ++ AkkaKernelPlugin.distSettings ++ Seq(
      name := "Akka Stress Test Node",
      distJvmOptions in Dist := "-Xms512M -Xmx2048M -Xss1M"
    )
  )

  override def rootProject = Some(node)

}

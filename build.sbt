name := "knobs-reader"
organization := "org.sazabi"
scalaVersion := "2.11.8"

version := "0.0.1-SNAPSHOT"

resolvers += Resolver.bintrayRepo("oncue", "releases")

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.7.0",
  "oncue.knobs" %% "core" % "3.6.1a",
  "com.chuusai" %% "shapeless" % "2.3.1",
  "org.scalatest" %% "scalatest" % "3.0.0-M15" % "test")

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xlint",
  "-language:implicitConversions",
  "-Ybackend:GenBCode",
  "-Ydelambdafy:method",
  "-target:jvm-1.8")

parallelExecution in Global := false

releasePublishArtifactsAction := PgpKeys.publishSigned.value
publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/solar/knobs-reader</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt"</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:solar/knobs-reader.git</url>
    <connection>scm:git:git@github.com:solar/knobs-reader.git</connection>
  </scm>
  <developers>
    <developer>
      <id>solar</id>
      <name>Shinpei Okamura</name>
      <url>https://github.com/solar</url>
    </developer>
  </developers>)

name := "chariot-core"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "mysql" % "mysql-connector-java" % "5.1.18",
  "com.google.code.gson" % "gson" % "1.7.1",
  "org.docx4j" % "docx4j" % "3.0.0" exclude("org.slf4j","slf4j-log4j12") exclude("log4j", "log4j"),
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.0",
  "commons-io" % "commons-io" % "2.0.1", 
  "commons-codec" % "commons-codec" % "1.7",
  "com.google.guava" % "guava" % "14.0"
)     

play.Project.playJavaSettings

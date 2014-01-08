name := "chariot-model"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "mysql" % "mysql-connector-java" % "5.1.18",
  "com.google.code.gson" % "gson" % "1.7.1"
)     

play.Project.playJavaSettings

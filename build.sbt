import play.Project._

name := "chariot"

version := "1.0"

libraryDependencies ++= Seq(javaJdbc, javaEbean)  

libraryDependencies +="mysql" % "mysql-connector-java" % "5.1.18"
 
libraryDependencies += "commons-io" % "commons-io" % "2.0.1"

libraryDependencies +="commons-codec" % "commons-codec" % "1.7"  

playJavaSettings

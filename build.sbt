import play.Project._

name := "chariot"

version := "1.0"

libraryDependencies ++= Seq(javaJdbc, javaEbean)     

playJavaSettings

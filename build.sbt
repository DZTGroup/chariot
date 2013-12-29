import play.Project._

name := "chariot"

version := "1.0"

libraryDependencies ++= Seq(
	javaJdbc, 
	javaEbean,
	cache,
	 "mysql" % "mysql-connector-java" % "5.1.18",
  "com.google.code.gson" % "gson" % "1.7.1"
)

libraryDependencies += "commons-io" % "commons-io" % "2.0.1"

libraryDependencies +="commons-codec" % "commons-codec" % "1.7"

playJavaSettings


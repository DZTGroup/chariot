import play.Project._

name := "chariot"

version := "1.0"

libraryDependencies ++= Seq(
	javaJdbc, 
	javaEbean,
	cache,
	 "mysql" % "mysql-connector-java" % "5.1.18"
)

playJavaSettings


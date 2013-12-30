import play.Project._

name := "chariot"

version := "1.0"

libraryDependencies ++= Seq(
	javaJdbc, 
	javaEbean,
	cache,
	"mysql" % "mysql-connector-java" % "5.1.18",
	"com.google.code.gson" % "gson" % "1.7.1",
	"org.docx4j" % "docx4j" % "3.0.0",
	"org.springframework" % "spring-context" % "4.0.0.RELEASE",
	"commons-io" % "commons-io" % "2.0.1",
	"commons-codec" % "commons-codec" % "1.7"
)

playJavaSettings


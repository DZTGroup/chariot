import play.Project._

name := "chariot-admin"

version := "1.0"

libraryDependencies ++= Seq(
	javaJdbc, 
	javaEbean,
	cache,
	"com.google.code.gson" % "gson" % "1.7.1",
	"commons-io" % "commons-io" % "2.0.1",
	"commons-codec" % "commons-codec" % "1.7"
)

playJavaSettings

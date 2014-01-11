import play.Project._

name := "chariot"

version := "1.0-SNAPSHOT"

play.Project.playJavaSettings

lazy val core =project.in(file("chariot-core"))
lazy val model=project.in(file("chariot-model"))
lazy val admin=project.in(file("chariot-admin")).dependsOn(core,model)
lazy val web  =project.in(file("chariot-web")).dependsOn(core,model)


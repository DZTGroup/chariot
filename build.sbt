import play.Project._

name := "chariot"

version := "1.0-SNAPSHOT"

play.Project.playJavaSettings

lazy val model=project.in(file("chariot-model"))
lazy val core =project.in(file("chariot-core")).dependsOn(model)
lazy val admin=project.in(file("chariot-admin")).dependsOn(core)
lazy val web  =project.in(file("chariot-web")).dependsOn(core)


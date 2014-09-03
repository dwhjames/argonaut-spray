
organization in ThisBuild := "com.pellucid"

licenses in ThisBuild += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalaVersion in ThisBuild := "2.11.2"

crossScalaVersions in ThisBuild := Seq("2.10.4", "2.11.2")

scalacOptions in ThisBuild ++= Seq("-feature", "-deprecation", "-unchecked")


lazy val argonautSpray = project in file(".")

name := "argonaut-spray"

libraryDependencies ++= Seq(
  "io.argonaut"         %%  "argonaut"    % "6.1-M4"  % "provided",
  "io.spray"            %%  "spray-http"  % "1.3.1"     % "provided",
  "io.spray"            %%  "spray-httpx" % "1.3.1"     % "provided",
  "com.typesafe.akka"   %%  "akka-actor"  % "2.3.4"     % "provided"
)

bintray.Plugin.bintrayPublishSettings

bintray.Keys.bintrayOrganization in bintray.Keys.bintray := Some("pellucid")

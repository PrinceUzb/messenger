import Dependencies._

ThisBuild / organization := "uz.scala"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version      := "1.0"

lazy val common = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/common"))
  .settings(libraryDependencies ++= commonPart)

lazy val server = (project in file("modules/server"))
  .settings(
    name := "messenger",
    libraryDependencies ++= coreLibraries
  )
  .settings(
    scalaJSProjects         := Seq(client),
    Assets / pipelineStages := Seq(scalaJSPipeline),
    pipelineStages          := Seq(digest, gzip),
    Compile / compile       := ((Compile / compile) dependsOn scalaJSPipeline).value)
  .enablePlugins(WebScalaJSBundlerPlugin)
  .dependsOn(common.jvm)

lazy val tests = project
  .in(file("modules/tests"))
  .configs(IntegrationTest)
  .settings(
    name := "messenger-test-suite",
    Defaults.itSettings,
    libraryDependencies ++= testLibraries ++ testLibraries.map(_ % Test)
  )
  .dependsOn(server)

lazy val client = (project in file("modules/client"))
  .settings(
    name := "client",
    scalaJSUseMainModuleInitializer := true,
    resolvers += Resolver.sonatypeRepo("releases"),
    libraryDependencies ++= Seq(
      "io.github.chronoscala"             %%% "chronoscala"   % "2.0.2",
      "com.github.japgolly.scalajs-react" %%% "core"          % Versions.scalaJsReact,
      "com.github.japgolly.scalajs-react" %%% "extra"         % Versions.scalaJsReact,
      "com.github.japgolly.scalacss"      %%% "ext-react"     % Versions.scalaCss,
      "io.circe"                          %%% "circe-core"    % Versions.circe,
      "io.circe"                          %%% "circe-parser"  % Versions.circe,
      "io.circe"                          %%% "circe-generic" % Versions.circe,
      "io.circe"                          %%% "circe-refined" % Versions.circe,
      "eu.timepit"                        %%% "refined"       % Versions.refined
    ),
    webpackEmitSourceMaps := false,
    Compile / npmDependencies ++= Seq(
      "react" -> Versions.reactJs,
      "react-dom" -> Versions.reactJs
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)
  .dependsOn(common.js)

lazy val messenger = (project in file(".")).aggregate(server, tests)


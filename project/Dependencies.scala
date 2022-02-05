import Dependencies.Libraries._
import sbt._

object Dependencies {
  object Versions {
    val cats          = "2.7.0"
    val catsEffect    = "3.3.5"
    val circe         = "0.14.1"
    val fs2           = "3.2.4"
    val http4s        = "0.23.9"
    val log4cats      = "2.2.0"
    val skunk         = "0.2.3"
    val logback       = "1.2.10"
    val ciris         = "2.3.2"
    val scalaCheck    = "1.15.4"
    val scalaTest     = "3.2.10"
    val scalaTestPlus = "3.2.10.0"
    val refined       = "0.9.28"
    val tsec          = "0.4.0"
    val bootstrap     = "5.1.3"
    val fontAwesome   = "5.15.4"
    val scalaJsReact  = "2.0.1"
    val scalaCss      = "1.0.0"
    val reactJs       = "17.0.2"
    val redis4cats    = "1.0.0"
  }

  object Libraries {
    def circe(artifact: String): ModuleID = "io.circe" %% artifact % Versions.circe

    def skunk(artifact: String): ModuleID = "org.tpolecat" %% artifact % Versions.skunk

    def ciris(artifact: String): ModuleID = "is.cir" %% artifact % Versions.ciris

    def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % Versions.http4s

    def refined(artifact: String): ModuleID = "eu.timepit" %% artifact % Versions.refined

    val circeCore    = circe("circe-core")
    val circeGeneric = circe("circe-generic")
    val circeParser  = circe("circe-parser")
    val circeRefined = circe("circe-refined")

    val skunkCore    = skunk("skunk-core")
    val skunkCirce   = skunk("skunk-circe")
    val skunkRefined = skunk("refined")

    val cirisCore    = ciris("ciris")
    val cirisRefined = ciris("ciris-refined")

    val http4sDsl    = http4s("http4s-dsl")
    val http4sCore   = http4s("http4s-core")
    val http4sServer = http4s("http4s-blaze-server")
    val http4sClient = http4s("http4s-blaze-client")
    val http4sCirce  = http4s("http4s-circe")
    val refinedType  = refined("refined")
    val refinedCats  = refined("refined-cats")

    val redis4catsEffects = "dev.profunktor" %% "redis4cats-effects" % Versions.redis4cats

    val tsecHttp4s = "io.github.jmcardon" %% "tsec-http4s" % Versions.tsec
    val cats       = "org.typelevel"      %% "cats-core"   % Versions.cats
    val catsEffect = "org.typelevel"      %% "cats-effect" % Versions.catsEffect
    val fs2        = "co.fs2"             %% "fs2-core"    % Versions.fs2

    val log4cats = "org.typelevel" %% "log4cats-slf4j"  % Versions.log4cats
    val logback  = "ch.qos.logback" % "logback-classic" % Versions.logback

    // Test
    val scalaCheck    = "org.scalacheck"    %% "scalacheck"      % Versions.scalaCheck
    val scalaTest     = "org.scalatest"     %% "scalatest"       % Versions.scalaTest
    val scalaTestPlus = "org.scalatestplus" %% "scalacheck-1-15" % Versions.scalaTestPlus

    val webjars: Seq[ModuleID] = Seq(
      "org.webjars" % "bootstrap"    % Versions.bootstrap,
      "org.webjars" % "font-awesome" % Versions.fontAwesome
    )
  }

  val circeLibs = Seq(circeCore, circeGeneric, circeParser, circeRefined)

  val catsLibs = Seq(cats, catsEffect)

  val http4sLibs = Seq(http4sDsl, http4sCore, http4sServer, http4sClient, http4sCirce)

  val cirisLibs = Seq(cirisRefined, cirisCore)

  val logLibs = Seq(log4cats, logback)

  val commonPart: Seq[ModuleID] = catsLibs ++ circeLibs :+ refinedType

  val coreLibraries: Seq[ModuleID] = commonPart ++ cirisLibs ++ http4sLibs ++ logLibs ++ webjars ++ Seq(
    skunkCore,
    skunkCirce,
    skunkRefined,
    fs2,
    tsecHttp4s,
    redis4catsEffects
  )

  val testLibraries = Seq(
    scalaCheck,
    scalaTest,
    scalaTestPlus
  )
}

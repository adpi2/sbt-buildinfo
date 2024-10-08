ThisBuild / organization := "com.eed3si9n"

ThisBuild / version := {
  val orig = (ThisBuild / version).value
  if (orig.endsWith("-SNAPSHOT")) "0.13.0-SNAPSHOT"
  else orig
}

val scala212 = "2.12.20"
val scala3 = "3.3.4"

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-buildinfo",
    scalaVersion := scala212,
    crossScalaVersions := Seq(scala212, scala3),
    scalacOptions ++= {
      onScalaVersion(
        scala212 = Seq("-Xlint", "-Xfatal-warnings", "-language:experimental.macros"),
        scala3 = Seq("-Wunused:all", "-Werror")
      ).value ++ Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")
    },
    libraryDependencies += onScalaVersion(
      scala212 = "org.scala-lang" % "scala-reflect" % scala212,
      scala3 = "org.scala-lang" % "scala-reflect" % "2.13.14"
    ).value,
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Xss4M", "-Dplugin.version=" + version.value),
    scriptedBufferLog := false,
    (pluginCrossBuild / sbtVersion) := onScalaVersion(scala212 = "1.3.9", scala3 = "2.0.0-M2").value
  )

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/sbt/sbt-buildinfo"),
    "scm:git@github.com:sbt/sbt-buildinfo.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "eed3si9n",
    name = "Eugene Yokota",
    email = "@eed3si9n",
    url = url("https://eed3si9n.com/")
  )
)
ThisBuild / description := "sbt plugin to generate build info"
ThisBuild / licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE"))
ThisBuild / homepage := Some(url("https://github.com/sbt/sbt-buildinfo"))

def onScalaVersion[T](scala212: T, scala3: T) = Def.setting {
  scalaBinaryVersion.value match {
    case "2.12" => scala212
    case _ => scala3
  }
}

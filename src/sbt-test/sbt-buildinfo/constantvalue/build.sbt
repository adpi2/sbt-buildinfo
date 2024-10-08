lazy val check = taskKey[Unit]("checks this plugin")

ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.3"
ThisBuild / homepage := Some(url("http://example.com"))
ThisBuild / licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE"))

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    name := "helloworld",
    TaskKey[Classpath]("someCp") := Seq(Attributed.blank(file("/tmp/f.txt"))),
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      BuildInfoKey.map(version) { case (n, v) => "projectVersion" -> v.toDouble },
      scalaVersion,
      ivyXML,
      homepage,
      licenses,
      apiMappings,
      isSnapshot,
      "year" -> 2012,
      "sym" -> 'Foo,
      "now" -> java.time.LocalDate.parse("2021-11-02"),
      "instant" -> java.time.Instant.parse("2021-11-02T01:23:45.678Z"),
      BuildInfoKey.action("buildTime") { 1234L },
      TaskKey[Classpath]("someCp"),
      target),
    buildInfoOptions ++= Seq(
      BuildInfoOption.Traits("traits.MyCustomTrait"),
      BuildInfoOption.ConstantValue,
      BuildInfoOption.ImportScalaPredef,
    ),
    buildInfoPackage := "hello",
    scalacOptions ++= Seq("-Xlint", "-Xfatal-warnings", "-Yno-imports"),
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
    check := {
      val sv = scalaVersion.value
      val f = (Compile / sourceManaged).value / "sbt-buildinfo" / ("%s.scala" format "BuildInfo")
      val lines = scala.io.Source.fromFile(f).getLines.toList
      lines match {
        case """// $COVERAGE-OFF$""" ::
          """package hello""" ::
          """""" ::
          """import scala.Predef._""" ::
          """""" ::
          """/** This object was generated by sbt-buildinfo. */""" ::
          """case object BuildInfo extends traits.MyCustomTrait {""" ::
          """  /** The value is "helloworld". */"""::
          """  final val name = "helloworld"""" ::
          """  /** The value is "0.1". */"""::
          """  final val version = "0.1"""" ::
          """  /** The value is 0.1. */"""::
          """  final val projectVersion = 0.1""" ::
          scalaVersionInfoComment ::
          scalaVersionInfo ::
          """  /** The value is scala.xml.NodeSeq.Empty. */""" ::
          """  val ivyXML: scala.xml.NodeSeq = scala.xml.NodeSeq.Empty""" ::
          """  /** The value is scala.Some(new java.net.URI("http://example.com").toURL). */""" ::
          """  val homepage: scala.Option[java.net.URL] = scala.Some(new java.net.URI("http://example.com").toURL)""" ::
          """  /** The value is scala.collection.immutable.Seq(("MIT License" -> new java.net.URI("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE").toURL)). */""" ::
          """  val licenses: scala.collection.immutable.Seq[(String, java.net.URL)] = scala.collection.immutable.Seq(("MIT License" -> new java.net.URI("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE").toURL))""" ::
          """  /** The value is Map(). */""" ::
          """  val apiMappings: Map[java.io.File, java.net.URL] = Map()""" ::
          """  /** The value is false. */""" ::
          """  final val isSnapshot = false""" ::
          """  /** The value is 2012. */""" ::
          """  final val year = 2012""" ::
          """  /** The value is scala.Symbol("Foo"). */""" ::
          """  final val sym = scala.Symbol("Foo")""" ::
          """  /** The value is java.time.LocalDate.parse("2021-11-02"). */""" ::
          """  val now: java.time.LocalDate = java.time.LocalDate.parse("2021-11-02")""" ::
          """  /** The value is java.time.Instant.parse("2021-11-02T01:23:45.678Z"). */""" ::
          """  val instant: java.time.Instant = java.time.Instant.parse("2021-11-02T01:23:45.678Z")""" ::
          """  /** The value is 1234L. */""" ::
          """  final val buildTime = 1234L""" ::
          """  /** The value is scala.collection.immutable.Seq(new java.io.File("/tmp/f.txt")). */""" ::
          """  val someCp: scala.collection.immutable.Seq[java.io.File] = scala.collection.immutable.Seq(new java.io.File("/tmp/f.txt"))""" ::
          targetInfoComment ::
          targetInfo :: // """
          """  override val toString: String = {""" ::
          """    "name: %s, version: %s, projectVersion: %s, scalaVersion: %s, ivyXML: %s, homepage: %s, licenses: %s, apiMappings: %s, isSnapshot: %s, year: %s, sym: %s, now: %s, instant: %s, buildTime: %s, someCp: %s, target: %s".format(""" ::
          """      name, version, projectVersion, scalaVersion, ivyXML, homepage, licenses, apiMappings, isSnapshot, year, sym, now, instant, buildTime, someCp, target""" ::
          """    )""" ::
          """  }""" ::
          """}""" ::
          """// $COVERAGE-ON$""" :: Nil  if (targetInfo contains "target: java.io.File = new java.io.File(") &&
          (scalaVersionInfo.trim == s"""final val scalaVersion = "$sv"""") => ()
        case _ => sys.error("unexpected output: \n" + lines.mkString("\n"))
      }
      ()
    }
  )

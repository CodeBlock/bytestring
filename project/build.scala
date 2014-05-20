import sbt._
import Keys._
import Tools.onVersion

object build extends Build {
  type Sett = Project.Setting[_]

  val scalazVersion = "7.0.6"

  lazy val standardSettings = Defaults.defaultSettings ++ List[Sett](
    organization := "org.purefn"
  , scalaVersion := "2.10.4"
  , crossScalaVersions := List("2.9.2", "2.9.3", "2.10.4", "2.11.0")
  , resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases"
  , scalacOptions <++= (scalaVersion).map((sv: String) ⇒ List("-deprecation", "-unchecked", "-Ywarn-value-discard") ++ (if(sv.contains("2.10") || sv.contains("2.11")) None else Some("-Ydependent-method-types")))
  , scalacOptions in (Compile, doc) <++= (baseDirectory in LocalProject("bytestring")).map {
      bd ⇒ List("-sourcepath", bd.getAbsolutePath, "-doc-source-url", "https://github.com/purefn/bytestring/€{FILE_PATH}.scala")
    }
  , testOptions in Test += Tests.Argument("showtimes")
  )

  lazy val bytestring = Project(
    id = "bytestring"
  , base = file(".")
  , settings = standardSettings ++ Unidoc.settings ++ List[Sett](
      name := "bytestring"
    , libraryDependencies ++= List(
        "org.scalaz" %% "scalaz-core" % scalazVersion
      , "org.scalaz" %% "scalaz-effect" % scalazVersion
      , "org.scalaz" %% "scalaz-iteratee" % scalazVersion
      )
    )
  , aggregate = List(scalacheckBinding, tests)
  )

  lazy val scalacheckBinding: Project = Project(
    id           = "scalacheck-binding"
  , base         = file("scalacheck-binding")
  , dependencies = List(bytestring)
  , settings     = standardSettings ++ List[Sett](
      name := "bytestring-scalacheck-binding"
    , libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.10.0"
    )
  )

  lazy val tests: Project = Project(
    id           = "tests"
  , base         = file("tests")
  , dependencies = List(bytestring, scalacheckBinding % "test")
  , settings     = standardSettings ++ List[Sett](
      name := "bytestring-tests"
    , libraryDependencies <++=
        onVersion(
          all = Seq("org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion % "test")
        , on292 = Seq("org.specs2" %% "specs2" % "1.12.4.1" % "test")
        , on210 = Seq("org.specs2" %% "specs2" % "1.14" % "test")
        , on211 = Seq("org.specs2" %% "specs2" % "2.3.11" % "test")
        )
      )
  )
}

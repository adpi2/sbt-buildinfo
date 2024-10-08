package sbtbuildinfo

import sbt._
import sbt.Scoped._

import scala.reflect.ClassTag

type BuildInfoKey = BuildInfoKey.Entry[?]
object BuildInfoKey {
  // appendSeqImplicit requires a Conversion[V, A1]
  given [A]: Conversion[SettingKey[A], BuildInfoKey] = BuildInfoKey.sbtbuildinfoSettingEntry(_)
  given [A: ClassTag]: Conversion[(String, A), BuildInfoKey] = BuildInfoKey.sbtbuildinfoConstantEntry(_)
  given [A: ClassTag]: Conversion[sbt.Task[A], BuildInfoKey] = BuildInfoKey.sbtbuildinfoTaskValueEntry(_)

  implicit def sbtbuildinfoSettingEntry[A](key: SettingKey[A]): Entry[A] = Setting(key)
  inline implicit def sbtbuildinfoTaskEntry[A](inline key: TaskKey[A]): Entry[A] = ${ BuildInfoKeyMacros.taskImpl('key) }
  implicit def sbtbuildinfoConstantEntry[A: ClassTag](tuple: (String, A)): Entry[A] = Constant(tuple)(getManifest)
  implicit def sbtbuildinfoTaskValueEntry[A: ClassTag](task: sbt.Task[A]): Entry[A] = TaskValue(task)(getManifest)

  def apply[A](key: SettingKey[A]): Entry[A] = Setting(key)
  inline def apply[A](inline key: TaskKey[A]): Entry[A] = ${ BuildInfoKeyMacros.taskImpl('key) }
  def apply[A: ClassTag](tuple: (String, A)): Entry[A] = Constant(tuple)(getManifest)
  def map[A, B: ClassTag](from: Entry[A])(fun: ((String, A)) => (String, B)): Entry[B] = BuildInfoKey.Mapped(from, fun)(getManifest)
  def action[A: ClassTag](name: String)(fun: => A): Entry[A] = Action(name, () => fun)(getManifest)

  private def getManifest[A](using classTag: ClassTag[A]): Class[A] = classTag.runtimeClass.asInstanceOf[Class[A]]

  @deprecated("use += (x: BuildInfoKey) instead", "0.10.0")
  def of[A](x: BuildInfoKey.Entry[A]): BuildInfoKey.Entry[A] = x
  @deprecated("use ++= Seq[BuildInfoKey](...) instead", "0.10.0")
  def ofN(xs: BuildInfoKey*): Seq[BuildInfoKey] = xs

  def outOfGraphUnsafe[A](key: TaskKey[A]): Entry[A] = Task(key)

  private[sbtbuildinfo] final case class Setting[A](scoped: SettingKey[A]) extends Entry[A] {
    def manifest = scoped.key.tag.typeArg.asInstanceOf[Class[A]]
  }
  private[sbtbuildinfo] final case class Task[A](scoped: TaskKey[A]) extends Entry[A] {
    def manifest = scoped.key.tag.typeArg.asInstanceOf[Class[A]]
  }

  private[sbtbuildinfo] final case class TaskValue[A](task: sbt.Task[A])(val manifest: Class[A]) extends Entry[A]

  private[sbtbuildinfo] final case class Constant[A](tuple: (String, A))(val manifest: Class[A]) extends Entry[A]

  private[sbtbuildinfo] final case class Mapped[A, B](from: Entry[A], fun: ((String, A)) => (String, B))(val manifest: Class[B])
  extends Entry[B]

  private[sbtbuildinfo] final case class Action[A](name: String, fun: () => A)(val manifest: Class[A])
  extends Entry[A]

  sealed trait Entry[A] {
    private[sbtbuildinfo] def manifest: Class[A]
  }
}

// flatMap has been renamed flatMapN in sbt 2.x
// TODO add flatMapN alias in sbt 1.x or flatMap alias in sbt 2.x
extension [A1 <: Tuple] (tuple: RichTaskables[A1])
  private def flatMap[A2](f: tuple.Fun[[X] =>> X,  Task[A2]]): Def.Initialize[Task[A2]] = tuple.flatMapN(f)

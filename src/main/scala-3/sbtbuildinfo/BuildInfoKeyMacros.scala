package sbtbuildinfo

import scala.quoted.*
import BuildInfoKey.*
import sbt.TaskKey

object BuildInfoKeyMacros:
  def taskImpl[A: Type](key: Expr[TaskKey[A]])(using Quotes): Expr[Entry[A]] =
    '{
      TaskValue[A]($key.taskValue)($key.key.tag.typeArg.asInstanceOf[Class[A]])
    }

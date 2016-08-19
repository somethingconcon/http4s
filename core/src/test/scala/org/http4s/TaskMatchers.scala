/* Derived from https://raw.githubusercontent.com/etorreborre/specs2/c0cbfc71390b644db1a5deeedc099f74a237ebde/matcher-extra/src/main/scala-scalaz-7.0.x/org/specs2/matcher/TaskMatchers.scala
 * License: https://raw.githubusercontent.com/etorreborre/specs2/master/LICENSE.txt
 */
package org.http4s

import scala.concurrent.TimeoutException
import scala.concurrent.duration.Duration

import fs2.Task
import org.specs2._
import org.specs2.matcher._
import org.specs2.matcher.ValueChecks._

/**
 * Matchers for scalaz.concurrent.Task
 */
trait TaskMatchers {
  // This comes from a private trait in real TaskMatchers
  implicit class NotNullSyntax(s: String) {
    def notNull: String =
      Option(s).getOrElse("null")
  }

  def returnOk[T]: TaskMatcher[T] =
    attemptRun(ValueCheck.alwaysOk, None)

  def returnValue[T](check: ValueCheck[T]): TaskMatcher[T] =
    attemptRun(check, None)

  def returnBefore[T](duration: Duration): TaskMatcher[T] =
    attemptRun(ValueCheck.alwaysOk, Some(duration))

  private[specs2] def attemptRun[T](check: ValueCheck[T], duration: Option[Duration]): TaskMatcher[T] =
   TaskMatcher(check, duration)

  case class TaskMatcher[T](check: ValueCheck[T], duration: Option[Duration]) extends Matcher[Task[T]] {
    def apply[S <: Task[T]](e: Expectable[S]) = {
      duration match {
        case Some(d) => e.value.attemptRunFor(d).fold(failedAttemptWithTimeout(e, d), checkResult(e))
        case None    => e.value.attemptRun.fold(failedAttempt(e), checkResult(e))
      }
    }

    def before(d: Duration): TaskMatcher[T] =
      copy(duration = Some(d))

    def withValue(check: ValueCheck[T]): TaskMatcher[T] =
      copy(check = check)

    def withValue(t: T): TaskMatcher[T] =
      withValue(valueIsTypedValueCheck(t))

    private def failedAttemptWithTimeout[S <: Task[T]](e: Expectable[S], d: Duration)(t: Throwable): MatchResult[S] = {
      t match {
        case te: TimeoutException =>
          val message = s"Timeout after ${d.toMillis} milliseconds"
          result(false, message, message, e)

        case _ =>
          val message = "an exception was thrown "+t.getMessage.notNull+" "+t.getClass.getName
          result(false, message, message, e)
      }
    }

    private def failedAttempt[S <: Task[T]](e: Expectable[S])(t: Throwable): MatchResult[S] = {
      val message = "an exception was thrown "+t.getMessage.notNull
      result(false, message, message, e)
    }

    private def checkResult[S <: Task[T]](e: Expectable[S])(t: T): MatchResult[S] =
      result(check.check(t), e)

  }
}

object TaskMatchers extends TaskMatchers
package org.sazabi.knobs

import scala.concurrent.duration.Duration

import knobs._
import scalaz._, std.list._
import scalaz.syntax.traverse._

trait ReadAt[A] extends Serializable { self =>
  def read(config: Config, key: String): Result[A]

  /**
    * Map a function over this [[ReadAt]].
    */
  final def map[B](f: A => B): ReadAt[B] = new ReadAt[B] {
    final def read(c: Config, k: String): Result[B] = self.read(c, k).map(f)
  }

  /**
    * Monadically bind a function over this [[ReadAt]].
    */
  final def flatMap[B](f: A => ReadAt[B]): ReadAt[B] = new ReadAt[B] {
    final def read(c: Config, k: String): Result[B] =
      self.read(c, k).flatMap(f(_).read(c, k))
  }

  /**
    * Monadically bind a function(A => Result[B]) over this [[ReadAt]].
    */
  final def flatMapR[B](f: A => Result[B]): ReadAt[B] = new ReadAt[B] {
    final def read(c: Config, k: String): Result[B] = self.read(c, k).flatMap(f)
  }
}

object ReadAt {
  final def apply[A](f: (Config, String) => Result[A]): ReadAt[A] =
    new ReadAt[A] {
      final def read(c: Config, key: String) = f(c, key)
    }

  implicit final def readAt[A](implicit ev: Configured[A]): ReadAt[A] = new ReadAt[A] {
    def read(c: Config, key: String): Result[A] = c.env.get(key) match {
      case Some(v) =>
        ev(v) match {
          case Some(a) => \/-(a)
          case None    => -\/(FailedAt(key))
        }
      case None => -\/(NoSuchKey(key))
    }
  }

  implicit final def optionReadAt[A](implicit ev: ReadAt[A]): ReadAt[Option[A]] =
    new ReadAt[Option[A]] {
      def read(c: Config, key: String): Result[Option[A]] = c.env.get(key) match {
        case Some(v) => ev.read(c, key).fold(_ => \/-(None), a => \/-(Some(a)))
        case None    => \/-(None)
      }
    }

  /**
    * Scalaz type classes for RaedAt
    * @TODO test laws
    */
  implicit final val monadInstances: MonadError[ReadAt, ReaderError] =
    new MonadError[ReadAt, ReaderError] {
      final def bind[A, B](fa: ReadAt[A])(f: A => ReadAt[B]): ReadAt[B] =
        fa.flatMap(f)

      override final def map[A, B](fa: ReadAt[A])(f: A => B): ReadAt[B] =
        fa.map(f)

      final def point[A](a: => A): ReadAt[A] = new ReadAt[A] {
        final def read(c: Config, key: String) = \/-(a)
      }

      final def raiseError[A](e: ReaderError): ReadAt[A] = new ReadAt[A] {
        final def read(c: Config, key: String) = -\/(e)
      }

      final def handleError[A](fa: ReadAt[A])(f: ReaderError => ReadAt[A]): ReadAt[A] =
        new ReadAt[A] {
          final def read(c: Config, key: String) =
            fa.read(c, key).recoverWith[ReaderError, A] {
              case e =>
                f(e).read(c, key)
            }
        }
    }
}

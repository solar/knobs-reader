package org.sazabi.knobs

import knobs.Config
import scalaz._

/**
  * Read A from (sub-)configs.
  */
trait Reader[A] extends Serializable { self =>
  def read(c: Config): Result[A]

  /**
    * Map a function over this [[Reader]].
    */
  final def map[B](f: A => B): Reader[B] = new Reader[B] {
    final def read(c: Config): Result[B] = self.read(c).map(f)
  }

  /**
    * Monadically bind a function over this [[Reader]].
    */
  final def flatMap[B](f: A => Reader[B]): Reader[B] = new Reader[B] {
    final def read(c: Config): Result[B] =
      self.read(c).flatMap(f(_).read(c))
  }

  final def flatMapR[B](f: A => Result[B]): Reader[B] = new Reader[B] {
    final def read(c: Config): Result[B] = self.read(c).flatMap(f)
  }
}

object Reader {
  final def apply[A](implicit ev: Reader[A]): Reader[A] = ev

  final def apply[A](f: Config => Result[A]): Reader[A] = new Reader[A] {
    def read(c: Config) = f(c)
  }

  implicit final def optionReader[A](implicit ev: Reader[A]): Reader[Option[A]] =
    new Reader[Option[A]] {
      final def read(c: Config): Result[Option[A]] =
        ev.read(c).fold(_ => \/-(None), a => \/-(Some(a)))
    }

  /**
    * Scalaz type classes for Raeder
    * @TODO test laws
    */
  implicit final val monadInstances: MonadError[Reader, ReaderError] =
    new MonadError[Reader, ReaderError] {
      final def bind[A, B](fa: Reader[A])(f: A => Reader[B]): Reader[B] = fa.flatMap(f)

      override final def map[A, B](fa: Reader[A])(f: A => B): Reader[B] = fa.map(f)

      final def point[A](a: => A): Reader[A] = new Reader[A] {
        final def read(c: Config) = \/-(a)
      }

      final def raiseError[A](e: ReaderError): Reader[A] = new Reader[A] {
        final def read(c: Config) = -\/(e)
      }

      final def handleError[A](fa: Reader[A])(f: ReaderError => Reader[A]): Reader[A] =
        new Reader[A] {
          final def read(c: Config) =
            fa.read(c).recoverWith[ReaderError, A] {
              case e =>
                f(e).read(c)
            }
        }
    }
}

package org.sazabi.knobs

import knobs.{ CfgValue, Config, Configured }
import scalaz._

trait Reader[A] extends Serializable { self =>
  def read(c: Config): ConfigException \/ A

  /**
    * Map a function over this [[Reader]].
    */
  final def map[B](f: A => B): Reader[B] = new Reader[B] {
    final def read(c: Config): ConfigException \/ B = self.read(c).map(f)
  }

  /**
    * Monadically bind a function over this [[Reader]].
    */
  final def flatMap[B](f: A => Reader[B]): Reader[B] = new Reader[B] {
    final def read(c: Config): ConfigException \/ B = self.read(c).flatMap(f(_).read(c))
  }
}

object Reader {
  def apply[A](f: Config => ConfigException \/ A): Reader[A] = new Reader[A] {
    def read(config: Config) = f(config)
  }

  def fromConfigured[A](key: String)(implicit ev: Configured[A]): Reader[A] =
    apply(_.lookup(key).map(\/-(_)).getOrElse(-\/(ReadException())))
}

package org.sazabi.knobs

import knobs.Config
import scalaz.\/
import scalaz.concurrent.Task
import shapeless._

trait Implicits {
  implicit val ToConfigOps: Config => ConfigOps = new ConfigOps(_)
}

class ConfigOps(val underlying: Config) extends AnyVal {
  def readAs[A](implicit ev: Reader[A]): Result[A] = ev.read(underlying)
  def readSubAs[A](key: String)(implicit ev: Reader[A]): Result[A] =
    ev.read(underlying.subconfig(key))

  def readAsT[A](implicit ev: Reader[A]): Task[A] =
    Task.fromDisjunction(ev.read(underlying).leftMap(ReaderErrorException))

  def readSubAsT[A](key: String)(implicit ev: Reader[A]): Task[A] =
    Task.fromDisjunction(
      ev.read(underlying.subconfig(key)).leftMap(ReaderErrorException))
}

object Implicits extends Implicits

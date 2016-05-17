package org.sazabi.knobs

import knobs.Config
import scalaz.\/
import shapeless._

object Implicits {
  implicit class ConfigOps(val value: Config) extends AnyVal {
    def readAs[A](implicit ev: Reader[A]): Result[A] = ev.read(value)
  }
}

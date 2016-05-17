package org.sazabi

import shapeless.Lazy
import scalaz.\/

package object knobs {
  type Result[+A] = ReaderError \/ A

  object semiauto {
    def deriveReader[A](implicit reader: Lazy[DerivedReader[A]]): Reader[A] = reader.value
  }
}

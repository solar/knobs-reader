package org.sazabi

import scalaz.\/
import shapeless.Lazy

package object knobs {
  type Result[+A] = ReaderError \/ A

  object semiauto {
    def deriveReader[A](implicit reader: Lazy[DerivedReader[A]]): Reader[A] =
      reader.value
  }
}

package org.sazabi

import shapeless.Lazy

package object knobs {
  object semiauto {
    def deriveReader[A](implicit reader: Lazy[DerivedReader[A]]): Reader[A] = reader.value
  }
}

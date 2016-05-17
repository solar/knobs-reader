package org.sazabi.knobs

import knobs.{ Config, Configured }
import scalaz._
import shapeless._
import shapeless.labelled.{ field, FieldType }

trait HListReader[L <: HList] extends Reader[L]

object HListReader {
  def apply[L <: HList](implicit readAs: HListReader[L]): HListReader[L] = readAs

  implicit def hnilReader: HListReader[HNil] = new HListReader[HNil] {
    def read(c: Config) = \/-(HNil)
  }

  implicit def hconsConfiguredReader[K <: Symbol, H, T <: HList](
      implicit key: Witness.Aux[K],
      headEv: Strict[Configured[H]],
      tailReader: HListReader[T]): HListReader[FieldType[K, H] :: T] =
    new HListReader[FieldType[K, H] :: T] {
      def read(c: Config) = {
        for {
          h <- c.lookup[H](key.value.name)(headEv.value)
                .map(\/-(_))
                .getOrElse(-\/(ReadException()))
          t <- tailReader.read(c)
        } yield field[K](h) :: t
      }
    }

  implicit def hconsReaderReader[K <: Symbol, H, T <: HList](
      implicit key: Witness.Aux[K],
      headEv: Strict[Reader[H]],
      tailReader: HListReader[T]): HListReader[FieldType[K, H] :: T] =
    new HListReader[FieldType[K, H] :: T] {
      def read(c: Config) = {
        for {
          h <- headEv.value.read(c.subconfig(key.value.name))
          t <- tailReader.read(c)
        } yield field[K](h) :: t
      }
    }
}

trait DerivedReader[A] extends Reader[A]

object DerivedReader {
  implicit def derivedReader[A, L <: HList](
      implicit gen: LabelledGeneric.Aux[A, L],
      readAs: Lazy[HListReader[L]]): DerivedReader[A] =
    new DerivedReader[A] {
      def read(c: Config) = readAs.value.read(c).map(gen.from(_))
    }
}

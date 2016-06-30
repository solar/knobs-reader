package org.sazabi.knobs

import knobs.Config
import org.scalatest._
import scalaz._

import org.sazabi.knobs.Implicits._
import org.sazabi.knobs.semiauto._

class ReaderSpec extends FlatSpec {
  val config = Config.parse("""
    | nested {
    |   id = 1
    |
    |   simple {
    |     value = "simple value"
    |   }
    |
    |   composed {
    |     id = 123
    |     name = "my name"
    |   }
    |
    |   ignored {
    |     hoge = true
    |     fuga = "value"
    |   }
    | }
    | optional {
    |   a = "hoge"
    |   c = true
    | }
    |""".stripMargin).getOrElse(Config.empty)

  case class Simple(value: String)
  object Simple {
    implicit val reader = deriveReader[Simple]
  }

  case class Composed(id: Int, name: String)
  object Composed {
    implicit val reader = deriveReader[Composed]
  }

  case class Nested(simple: Simple, id: Int, composed: Composed)
  object Nested {
    implicit val reader = deriveReader[Nested]
  }

  case class Optional(a: String, b: Option[Int], c: Option[Boolean])
  object Optional {
    implicit val reader = deriveReader[Optional]
  }

  behavior of "Reader[A]"

  it should "read A from knobs.Config" in {
    val config = Config.parse("""
      |value = "my value"
      |""".stripMargin).getOrElse(Config.empty)

    assert(config.readAs[Simple] == \/-(Simple("my value")))
  }

  it should "read A from subconfig" in {
    assert(config.readSubAs[Simple]("nested.simple") == \/-(
            Simple("simple value")))
    assert(config.readSubAs[Composed]("nested.composed") == \/-(
            Composed(123, "my name")))
  }

  it should "read nested values" in {
    assert(
        config.readSubAs[Nested]("nested") == \/-(
            Nested(simple = Simple("simple value"),
                   id = 1,
                   composed = Composed(id = 123, name = "my name"))))
  }

  it should "read optional values" in {
    assert(config.readSubAs[Optional]("optional") == \/-(
            Optional(a = "hoge", b = None, c = Some(true))))

    assert(config.readSubAs[Option[Optional]]("nested.optional") == \/-(None))
  }

  it should "fail on invalid reader" in {
    val e = config.readSubAs[Simple]("nested")
    assert(e == -\/(NoSuchKey("value")))
  }

  it should "map to Reader[B]" in {
    val mapped = Nested.reader.map(_.simple)
    assert(config.readSubAs[Simple]("simple") == mapped.read(
            config.subconfig("simple")))
  }

  it should "flatMap to Reader[B]" in {
    val mapped = Nested.reader.map(_.simple)
    assert(config.readSubAs[Simple]("simple") == mapped.read(
            config.subconfig("simple")))
  }

  it should "flatMapR to Reader[B]" in {
    val mapped = Nested.reader.map(_.simple)
    assert(config.readSubAs[Simple]("simple") == mapped.read(
            config.subconfig("simple")))
  }
}

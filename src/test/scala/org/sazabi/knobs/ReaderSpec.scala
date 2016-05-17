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

  "Reader[A]" should "read A from knobs.Config" in {
    val config = Config.parse("""
      |value = "my value"
      |""".stripMargin).getOrElse(Config.empty)

    assert(config.readAs[Simple] == \/-(Simple("my value")))
  }

  it should "read A from subconfig" in {
    assert(
      config.subconfig("nested.simple").readAs[Simple] == \/-(Simple("simple value")))
    assert(config.subconfig("nested.composed").readAs[Composed] == \/-(
        Composed(123, "my name")))
  }

  it should "read nested values" in {
    assert(
      config.subconfig("nested").readAs[Nested] == \/-(
        Nested(simple = Simple("simple value"),
               id = 1,
               composed = Composed(id = 123, name = "my name"))))
  }

  it should "fail on invalid reader" in {
    assert(config.readAs[Simple] == -\/(ReadException("read failure")))
  }
}

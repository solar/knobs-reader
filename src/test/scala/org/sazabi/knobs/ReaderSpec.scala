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
    assert(config.readSubAs[Simple]("nested.simple") == \/-(Simple("simple value")))
    assert(
      config.readSubAs[Composed]("nested.composed") == \/-(Composed(123, "my name")))
  }

  it should "read nested values" in {
    assert(
      config.readSubAs[Nested]("nested") == \/-(
        Nested(simple = Simple("simple value"),
               id = 1,
               composed = Composed(id = 123, name = "my name"))))
  }

  it should "fail on invalid reader" in {
    val e = config.readSubAs[Simple]("nested")
    assert(e == -\/(FailedAt("value", Some(config.subconfig("nested")))))

    assert(
      e.swap
        .map(_.message == "failed at 'value' in Config(Map(composed.name -> CfgText(my name), composed.id -> CfgNumber(123.0), ignored.fuga -> CfgText(value), id -> CfgNumber(1.0), simple.value -> CfgText(simple value), ignored.hoge -> CfgBool(true)))")
        .getOrElse(false))
  }

  it should "map to Reader[B]" in {
    val mapped = Nested.reader.map(_.simple)
    assert(config.readSubAs[Simple]("simple") == mapped.read(config.subconfig("simple")))
  }

  it should "flatMap to Reader[B]" in {
    val mapped = Nested.reader.map(_.simple)
    assert(config.readSubAs[Simple]("simple") == mapped.read(config.subconfig("simple")))
  }

  it should "flatMapR to Reader[B]" in {
    val mapped = Nested.reader.map(_.simple)
    assert(config.readSubAs[Simple]("simple") == mapped.read(config.subconfig("simple")))
  }
}

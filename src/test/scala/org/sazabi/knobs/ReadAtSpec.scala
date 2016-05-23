package org.sazabi.knobs

import knobs.Config
import org.scalatest._
import scalaz._

import org.sazabi.knobs.Implicits._
import org.sazabi.knobs.semiauto._

class ReadAtSpec extends FlatSpec {
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

  "ReadAt[A]" should "read a value on a key" in {
    assert(config.readAt[String]("nested.simple.value") == \/-("simple value"))
  }

  "ReadAt[Option[A]]" should "raed a optional value on a key" in {
    assert(config.readAt[Option[String]]("nonono") == \/-(None))
    assert(config.readAt[Option[String]]("optional.a") == \/-(Some("hoge")))
    assert(config.readAt[Option[Int]]("optional.a") == \/-(None))
  }
}

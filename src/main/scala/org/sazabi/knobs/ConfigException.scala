package org.sazabi.knobs

sealed abstract class ConfigException(message: String) extends RuntimeException(message)

case class ReadException(msg: String = "read failure") extends ConfigException(msg)

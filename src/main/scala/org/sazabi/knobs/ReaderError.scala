package org.sazabi.knobs

import knobs.Config

sealed abstract class ReaderError(val message: String)

case class FailedAt(key: String, config: Option[Config] = None) extends ReaderError(
  s"failed at '$key' in ${config.map(_.toString).getOrElse("-")}")

case class ReadFailure(override val message: String) extends ReaderError(message)

case class ReaderErrorException(error: ReaderError) extends RuntimeException

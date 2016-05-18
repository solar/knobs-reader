package org.sazabi.knobs

import knobs.Config

sealed trait ReaderError

case class FailedAt(key: String, config: Option[Config] = None) extends ReaderError
case class ReadFailure() extends ReaderError

case class ReaderErrorException(error: ReaderError) extends RuntimeException

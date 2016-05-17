package org.sazabi.knobs

sealed trait ReaderError

case class KeyNotFound() extends ReaderError
case class ReadFailure() extends ReaderError

package org.sazabi.knobs

import knobs.Config

sealed abstract class ReaderError(val message: String)

case class FailedAt(key: String) extends ReaderError(s"Failed to read at '$key'")

case class NoSuchKey(key: String)
    extends ReaderError(s"""Key "${ key }" was not found""")

case class UnexpectedType(key: String)
    extends ReaderError(s"""Key "${ key }" has unexpected type""")

case class ReadFailure(override val message: String) extends ReaderError(message)

case class ReaderErrorException(error: ReaderError) extends RuntimeException

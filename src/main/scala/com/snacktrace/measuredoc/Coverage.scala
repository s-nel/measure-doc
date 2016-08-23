package com.snacktrace.measuredoc

final case class Coverage(className: String, docType: Level, covered: Int, total: Int, missingCoverage: MissingCoverages)

final case class Coverages(value: Set[Coverage])

sealed trait Level

object Type extends Level
object Method extends Level
object Field extends Level

final case class MissingCoverages(value: Set[MissingCoverage])

final case class MissingCoverage(tag: Tag, param: Option[String] = None)

sealed trait Tag

object Text extends Tag
object Param extends Tag
object Return extends Tag
object Throws extends Tag
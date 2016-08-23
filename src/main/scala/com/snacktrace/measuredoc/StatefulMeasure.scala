package com.snacktrace.measuredoc

import com.puppycrawl.tools.checkstyle.api.{FileContents, DetailAST}

trait StatefulMeasure {
  def getMeasuredCoverages(): Coverages
  def resetMeasuredCoverages(): Unit
  def visitToken(ast: DetailAST): Unit
  def getTokens: Array[Int]
  def setFileContents(fileContents: FileContents): Unit
}

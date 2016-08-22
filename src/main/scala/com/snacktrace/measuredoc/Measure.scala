package com.snacktrace.measuredoc

import com.puppycrawl.tools.checkstyle.api.{AbstractCheck, FileSetCheck}

abstract class AbstractMeasure extends AbstractCheck {
  def getMeasuredCoverage(): Coverage
}

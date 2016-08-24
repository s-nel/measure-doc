package com.snacktrace.measuredoc.measures

import com.puppycrawl.tools.checkstyle.api.TextBlock

trait JavadocSummaryParser {
  def parse(textBlock: TextBlock): Option[Seq[String]]
}

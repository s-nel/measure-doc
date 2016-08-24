package com.snacktrace.measuredoc.measures.impl

import com.puppycrawl.tools.checkstyle.api.TextBlock
import com.puppycrawl.tools.checkstyle.utils.JavadocUtils
import com.snacktrace.measuredoc.measures.JavadocSummaryParser

import scala.collection.JavaConverters._

class DefaultJavadocSummaryParser extends JavadocSummaryParser {
  override def parse(textBlock: TextBlock): Option[Seq[String]] = {
    def removeFormattingFromSummary(textBlock: TextBlock, lastLineNo: Int): Option[Seq[String]] = {
      val lines = textBlock.getText.toSeq.zipWithIndex
        .filter(args => args._2 <= lastLineNo - textBlock.getStartLineNo)
        .map(_._1)
        .map { line =>
          line.replaceFirst("^\\s*/?\\*\\*?\\s*", "").trim
        }
        .filter(_.nonEmpty)
      if (lines.isEmpty) {
        None
      } else {
        Some(lines)
      }
    }

    val tags = JavadocUtils.getJavadocTags(textBlock, JavadocUtils.JavadocTagType.BLOCK).getValidTags.asScala.toSeq
    tags.sortWith((t1, t2) => t1.getLineNo <= t2.getLineNo).headOption match {
      case Some(tag) => removeFormattingFromSummary(textBlock, tag.getLineNo - 1)
      case None => removeFormattingFromSummary(textBlock, textBlock.getEndLineNo)
    }
  }
}

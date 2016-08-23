package com.snacktrace.measuredoc.checkstyle

import java.io.File
import java.nio.charset.Charset

import com.google.common.collect.Multimap
import com.puppycrawl.tools.checkstyle.utils.TokenUtils
import com.puppycrawl.tools.checkstyle.{TreeWalker, DefaultConfiguration}
import com.puppycrawl.tools.checkstyle.api.{FileContents, DetailAST, AbstractCheck, FileText}
import com.snacktrace.measuredoc.impl.DefaultCoveragesAggregator
import com.snacktrace.measuredoc.{CoveragesAggregator, StatefulMeasure, Coverages, Measurer}

import scala.concurrent.{ExecutionContext, Future}

class MeasurerCheckStyle(measures: Seq[StatefulMeasure],
  aggregator: CoveragesAggregator = new DefaultCoveragesAggregator(),
  charset:Charset = Charset.forName("UTF-8"))(implicit executionContext: ExecutionContext) extends Measurer {
  override def measure(files: Seq[File]): Future[Coverages] = Future {
    files.map { file =>
      val fileText = new FileText(file.getAbsoluteFile, charset.name())
      measures.map { measure =>
        val abstractCheck = new MeasureAbstractChecker(measure)
        val treeWalker = new TreeWalker()
        val treeWalkerCls = classOf[TreeWalker]

        val tokenToCheckField = treeWalkerCls.getDeclaredField("tokenToOrdinaryChecks")
        tokenToCheckField.setAccessible(true)
        val tokenToCheck = tokenToCheckField.get(treeWalker).asInstanceOf[Multimap[String, AbstractCheck]]

        val checksField = treeWalkerCls.getDeclaredField("ordinaryChecks")
        checksField.setAccessible(true)
        val checks = checksField.get(treeWalker).asInstanceOf[java.util.Set[AbstractCheck]]

        measure.getTokens.foreach { token =>
          tokenToCheck.put(TokenUtils.getTokenName(token), abstractCheck)
        }
        checks.add(abstractCheck)
        val contents = new FileContents(fileText)
        TreeWalker.parse(contents)
        measure.setFileContents(contents)
        treeWalker.process(file, fileText)
        val coverages = measure.getMeasuredCoverages()
        measure.resetMeasuredCoverages()
        coverages
      }.reduce(aggregator.aggregate)
    }.reduce(aggregator.aggregate)
  }

  class MeasureAbstractChecker(measure: StatefulMeasure) extends AbstractCheck {
    override def getDefaultTokens: Array[Int] = measure.getTokens
    override def getAcceptableTokens: Array[Int] = measure.getTokens
    override def getRequiredTokens: Array[Int] = Array.empty[Int]
    override def visitToken(detailAST: DetailAST): Unit = measure.visitToken(detailAST)
  }
}

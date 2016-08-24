package com.snacktrace.measuredoc.measures

import java._
import java.io.PrintWriter

import com.puppycrawl.tools.checkstyle.api._
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocTag
import com.puppycrawl.tools.checkstyle.utils.{CheckUtils, CommonUtils, JavadocUtils, ScopeUtils}
import com.snacktrace.measuredoc._
import com.snacktrace.measuredoc.measures.impl.DefaultJavadocSummaryParser

import scala.collection.JavaConversions._
import scala.collection.mutable.HashSet

class JavadocTypeMeasure(openAngleBracket: String = "<", closeAngleBracket: String = ">",
  javadocSummaryParser: JavadocSummaryParser = new DefaultJavadocSummaryParser()) extends AbstractCheck with StatefulMeasure {

  var coverage = HashSet.empty[Coverage]

  override def getTokens: Array[Int] = {
    return Array[Int](
      TokenTypes.INTERFACE_DEF,
      TokenTypes.CLASS_DEF,
      TokenTypes.ENUM_DEF,
      TokenTypes.ANNOTATION_DEF
    )
  }

  override def visitToken(ast: DetailAST): Unit = {
    super.visitToken(ast)
    if (shouldCover(ast)) {
      coverage += getDocCoverage(ast)
    }
  }

  private def shouldCover(ast: DetailAST): Boolean = {
    val mods: DetailAST = ast.findFirstToken(TokenTypes.MODIFIERS)
    val declaredScope: Scope = ScopeUtils.getScopeFromMods(mods)
    val customScope = if (ScopeUtils.isInInterfaceOrAnnotationBlock(ast)) {
      Scope.PUBLIC
    } else {
      declaredScope
    }
    val surroundingScope: Scope = ScopeUtils.getSurroundingScope(ast)
    customScope.isIn(Scope.PUBLIC) && (surroundingScope == null || surroundingScope.isIn(Scope.PUBLIC))
  }

  private def getDocCoverage(ast: DetailAST): Coverage = {
    val contents: FileContents = getFileContents
    val lineNo: Int = ast.getLineNo
    val className = ast.findFirstToken(TokenTypes.IDENT).getText
    val textBlock = contents.getJavadocBefore(lineNo)
    val typeParamNames = CheckUtils.getTypeParameterNames(ast)
    val total = 1 + typeParamNames.size
    val missingDoc = if (textBlock == null) {
      Seq(MissingCoverage(Text)) ++ typeParamNames.toIndexedSeq.map { paramName =>
        MissingCoverage(Param, Some(paramName))
      }
    } else {
      val tags = getJavadocTags(textBlock)
      val tagMissingCoverage = for {
        typeParamName <- typeParamNames
      } yield {
        checkTypeParamTag(tags, typeParamName)
      }
      val summaryMissingCoverage = checkSummary(textBlock)
      (tagMissingCoverage ++ Seq(summaryMissingCoverage)).collect {
        case Some(s) => s
      }
    }
    Coverage(className, Type, total - missingDoc.size, total, MissingCoverages(missingDoc.toSet))
  }

  private def checkTypeParamTag (tags: util.List[JavadocTag], typeParamName: String): Option[MissingCoverage] = {
    tags.find { tag =>
      tag.isParamTag && tag.getFirstArg.indexOf(s"${openAngleBracket}${typeParamName}${closeAngleBracket}") == 0
    } match {
      case Some(tag) =>
        val typeParamComment = tag.getFirstArg.substring(openAngleBracket.length + typeParamName.length + closeAngleBracket.length).trim
        if (typeParamComment.isEmpty) {
          Some(MissingCoverage(Param, Some(typeParamName)))
        } else {
          None
        }
      case None => Some(MissingCoverage(Param, Some(typeParamName)))
    }
  }

  private def getJavadocTags(textBlock: TextBlock): util.List[JavadocTag] = {
    val tags = JavadocUtils.getJavadocTags(textBlock, JavadocUtils.JavadocTagType.BLOCK)
    return tags.getValidTags
  }

  override def getMeasuredCoverages(): Coverages = {
    Coverages(coverage.toSet)
  }

  override def resetMeasuredCoverages(): Unit = {
    coverage.clear()
  }

  override def getDefaultTokens: Array[Int] = ???

  private def checkSummary(textBlock: TextBlock): Option[MissingCoverage] = {
    javadocSummaryParser.parse(textBlock) match {
      case None => Some(MissingCoverage(Text))
      case Some(t) => None
    }
  }
}

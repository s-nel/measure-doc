package com.snacktrace.measuredoc.measures

import java.util

import com.puppycrawl.tools.checkstyle.api._
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocTag
import com.puppycrawl.tools.checkstyle.utils.{JavadocUtils, CheckUtils, ScopeUtils}
import com.snacktrace.measuredoc._
import com.snacktrace.measuredoc.measures.impl.DefaultJavadocSummaryParser

import scala.annotation.tailrec
import scala.collection.mutable.HashSet
import scala.collection.JavaConversions._

class JavadocMethodMeasure(openAngleBracket: String = "<", closeAngleBracket: String = ">",
  javadocSummaryParser: JavadocSummaryParser = new DefaultJavadocSummaryParser()) extends AbstractCheck with StatefulMeasure {

  var coverage = HashSet.empty[Coverage]

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

  override def getTokens: Array[Int] =  Array(
    TokenTypes.METHOD_DEF,
    TokenTypes.CTOR_DEF
  )

  private def getDocCoverage(ast: DetailAST): Coverage = {
    val contents: FileContents = getFileContents
    val lineNo: Int = ast.getLineNo
    val className = ast.findFirstToken(TokenTypes.IDENT).getText
    val textBlock = contents.getJavadocBefore(lineNo)
    val typeParamNames = CheckUtils.getTypeParameterNames(ast)
    val paramNames = getParameterNames(ast)
    val exceptionNames = getExceptionNames(ast)
    val returnTypeExpected = if (hasNonVoidReturnType(ast)) {
      1
    } else {
      0
    }

    val total = 1 + typeParamNames.size + paramNames.size + exceptionNames.size + returnTypeExpected
    val missingDoc = if (textBlock == null) {
      Seq(MissingCoverage(Text)) ++ typeParamNames.toIndexedSeq.map { paramName =>
        MissingCoverage(Param, Some(paramName))
      } ++ paramNames.map { paramName =>
        MissingCoverage(Param, Some(paramName))
      } ++ (if (hasNonVoidReturnType(ast)) {
        Seq(MissingCoverage(Return))
      } else {
        Seq.empty
      }) ++ exceptionNames.map { exceptionName =>
        MissingCoverage(Throws, Some(exceptionName))
      }
    } else {
      val tags = getJavadocTags(textBlock)

      val missingTypeParams = for {
        typeParamName <- typeParamNames
      } yield {
        checkTypeParamTag(tags, typeParamName)
      }

      val missingParams = for {
        paramName <- paramNames
      } yield {
        checkParamTag(tags, paramName)
      }

      val missingThrows = for {
        exceptionName <- exceptionNames
      } yield {
        checkThrowsTag(tags, exceptionName)
      }

      val missingReturn = if (hasNonVoidReturnType(ast)) {
        checkReturnTag(tags)
      } else {
        None
      }

      val summaryMissingCoverage = checkSummary(textBlock)

      (missingTypeParams
        ++ Seq(summaryMissingCoverage)
        ++ Seq(missingReturn)
        ++ missingThrows
        ++ missingParams).collect {
        case Some(s) => s
      }
    }
    Coverage(className, Method, total - missingDoc.size, total, MissingCoverages(missingDoc.toSet))
  }

  private def checkTypeParamTag(tags: Seq[JavadocTag], typeParamName: String): Option[MissingCoverage] = {
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

  private def checkParamTag(tags: Seq[JavadocTag], paramName: String): Option[MissingCoverage] = {
    tags.find { tag =>
      tag.isParamTag && tag.getFirstArg.indexOf(paramName) == 0
    } match {
      case Some(tag) =>
        val paramComment = tag.getFirstArg.substring(paramName.length).trim
        if (paramComment.isEmpty) {
          Some(MissingCoverage(Param, Some(paramName)))
        } else {
          None
        }
      case None => Some(MissingCoverage(Param, Some(paramName)))
    }
  }

  private def checkThrowsTag(tags: Seq[JavadocTag], exceptionName: String): Option[MissingCoverage] = {
    tags.find { tag =>
      tag.isThrowsTag && tag.getFirstArg.indexOf(exceptionName) == 0
    } match {
      case Some(tag) =>
        val paramComment = tag.getFirstArg.substring(exceptionName.length).trim
        if (paramComment.isEmpty) {
          Some(MissingCoverage(Throws, Some(exceptionName)))
        } else {
          None
        }
      case None => Some(MissingCoverage(Throws, Some(exceptionName)))
    }
  }

  private def checkReturnTag(tags: Seq[JavadocTag]): Option[MissingCoverage] = {
    tags.find { tag =>
      tag.isReturnTag
    } match {
      case Some(tag) =>
        if (tag.getFirstArg.trim.isEmpty) {
          Some(MissingCoverage(Return))
        } else {
          None
        }
      case None => Some(MissingCoverage(Return))
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

  private def collectSiblings(sibling: Option[DetailAST]): Seq[DetailAST] = {
    sibling match {
      case Some(sibling) if sibling != null => Seq(sibling) ++ collectSiblings(Option(sibling.getNextSibling))
      case _ => Seq.empty
    }
  }

  private def getParameterNames(ast: DetailAST): Seq[String] = {
    collectSiblings(Option(ast.findFirstToken(TokenTypes.PARAMETERS)).map(_.getFirstChild))
      .filter(_.getType == TokenTypes.PARAMETER_DEF)
      .map(_.findFirstToken(TokenTypes.IDENT))
      .map(_.getText)
  }

  private def getExceptionNames(ast: DetailAST): Seq[String] = {
    collectSiblings(Option(ast.findFirstToken(TokenTypes.LITERAL_THROWS)).map(_.getFirstChild))
      .filter(_.getType == TokenTypes.IDENT)
      .map(_.getText)
  }

  private def hasNonVoidReturnType(ast: DetailAST): Boolean = {
    val typeToken = ast.findFirstToken(TokenTypes.TYPE)
    if (typeToken == null) {
      false
    } else {
      !typeToken.getFirstChild.getText.equals("void")
    }
  }
}

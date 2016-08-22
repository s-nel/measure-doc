package com.snacktrace.measuredoc

import com.puppycrawl.tools.checkstyle.api._
import com.puppycrawl.tools.checkstyle.checks.javadoc.{JavadocTags, JavadocTag, JavadocTagInfo}
import com.puppycrawl.tools.checkstyle.utils.{JavadocUtils, CheckUtils, ScopeUtils, CommonUtils}

import scala.collection.JavaConversions._
import scala.collection.mutable.HashSet

import java._

import scala.collection.mutable

class JavadocTypeMeasure(openAngleBracket: String = "<", closeAngleBracket: String = ">") extends AbstractMeasure {

  var coverage = HashSet.empty[Coverage]

  override def getAcceptableTokens: Array[Int] = {
    return Array[Int](
      TokenTypes.INTERFACE_DEF,
      TokenTypes.CLASS_DEF,
      TokenTypes.ENUM_DEF,
      TokenTypes.ANNOTATION_DEF
    )
  }

  override def getDefaultTokens: Array[Int] = {
    return getAcceptableTokens
  }

  override def visitToken(ast: DetailAST): Unit = {
    super.visitToken(ast)
    val mods: DetailAST = ast.findFirstToken(TokenTypes.MODIFIERS)
    val declaredScope: Scope = ScopeUtils.getScopeFromMods(mods)
    val customScope = if (ScopeUtils.isInInterfaceOrAnnotationBlock(ast)) {
      Scope.PUBLIC
    } else {
      declaredScope
    }
    val surroundingScope: Scope = ScopeUtils.getSurroundingScope(ast)
    if (customScope.isIn(Scope.PUBLIC) && (surroundingScope == null || surroundingScope.isIn(Scope.PUBLIC))) {
      coverage += getDocCoverage(ast)
    }
  }

  override def getRequiredTokens: Array[Int] = {
    return CommonUtils.EMPTY_INT_ARRAY
  }

  private def getDocCoverage(ast: DetailAST): Coverage = {
    val contents: FileContents = getFileContents
    val lineNo: Int = ast.getLineNo
    val textBlock = contents.getJavadocBefore(lineNo)
    val typeParamNames = CheckUtils.getTypeParameterNames(ast)
    val total = 1 + typeParamNames.size
    val missingDoc = if (textBlock == null) {
      Seq(MissingCoverage(Text)) ++ typeParamNames.toIndexedSeq.map { paramName =>
        MissingCoverage(Param, Some(paramName))
      }
    } else {
      val tags = getJavadocTags(textBlock)
      (for {
        typeParamName <- typeParamNames
      } yield {
        checkTypeParamTag(tags, typeParamName)
      }).collect {
        case Some(s) => s
      }
    }
    Coverage(Type, total - missingDoc.size, total, contents.getFileName, MissingCoverages(missingDoc.toSet))
  }

  private def checkTypeParamTag (tags: util.List[JavadocTag], typeParamName: String): Option[MissingCoverage] = {
    val found = tags.exists { tag =>
      tag.isParamTag && tag.getFirstArg.indexOf(s"${openAngleBracket}${typeParamName}${closeAngleBracket}") == 0
    }
    if (!found) {
      Some(MissingCoverage(Param, Some(typeParamName)))
    } else {
      None
    }
  }

  private def getJavadocTags(textBlock: TextBlock): util.List[JavadocTag] = {
    val tags = JavadocUtils.getJavadocTags(textBlock, JavadocUtils.JavadocTagType.BLOCK)
    return tags.getValidTags
  }

  override def getMeasuredCoverage(): Coverages = {
    Coverages(coverage.toSet)
  }
}

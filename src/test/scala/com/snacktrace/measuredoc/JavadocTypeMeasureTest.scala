package com.snacktrace.measuredoc

import java.io.File

import com.puppycrawl.tools.checkstyle.api.Scope
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocTypeCheck
import com.puppycrawl.tools.checkstyle.{DefaultConfiguration, BaseCheckTestSupport}
import org.scalatest.WordSpec

class JavadocTypeMeasureTest extends WordSpec with BaseCheckTestSupport {


  "JavadocTypeMeasure.visitToken" should {
    "record coverage" in {
      val checkConfig = new DefaultConfiguration(classOf[JavadocTypeMeasure].getName)
      val expected = Coverages(Set(Coverage(Type, 0, 3, "TypeParam.java",
        MissingCoverages(Set(
          MissingCoverage(Text),
          MissingCoverage(Param, Some("S")),
          MissingCoverage(Param, Some("T"))
        )))))
      verify(checkConfig, getPath("TypeParam.java"), expected)
    }
  }

  override def getPath(filename: String): String = {
    return new File("src/test/" + filename).getCanonicalPath()
  }


}

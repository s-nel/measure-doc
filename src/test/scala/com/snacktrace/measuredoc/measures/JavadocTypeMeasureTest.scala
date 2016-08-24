package com.snacktrace.measuredoc.measures

import java.io.File

import com.puppycrawl.tools.checkstyle.{BaseCheckTestSupport, DefaultConfiguration}
import com.snacktrace.measuredoc._
import com.snacktrace.measuredoc.javafiles.{TypeParamComplete, TypeParamPartial, TypeParamMissing}
import org.scalatest.WordSpec

class JavadocTypeMeasureTest extends WordSpec with BaseMeasureTestSupport {
  trait Fixture {
    val measure = new JavadocTypeMeasure()

    final case class TD(name: String, cls: Class[_], expected: Coverages)

    val testDatas = Seq(
      TD("missing type parameters and text",
        classOf[TypeParamMissing[_, _]],
        Coverages(Set(
          Coverage(
            classOf[TypeParamMissing[_, _]].getSimpleName,
            Type,
            0,
            3,
            MissingCoverages(Set(
              MissingCoverage(Text),
              MissingCoverage(Param, Some("S")),
              MissingCoverage(Param, Some("T"))
            ))
          )
        ))
      ),
      TD("partially missing type parameters and text",
        classOf[TypeParamPartial[_, _]],
        Coverages(Set(
          Coverage(
            classOf[TypeParamPartial[_, _]].getSimpleName,
            Type,
            1,
            3,
            MissingCoverages(Set(
              MissingCoverage(Text),
              MissingCoverage(Param, Some("T"))
            ))
          )
        ))
      ),
      TD("complete type javadoc",
        classOf[TypeParamComplete[_, _]],
        Coverages(Set(
          Coverage(
            classOf[TypeParamComplete[_, _]].getSimpleName,
            Type,
            3,
            3,
            MissingCoverages(Set.empty)
          )
        )))
    )
  }

  new Fixture {
    "JavadocTypeMeasure.visitToken" should {
      testDatas.foreach { td =>
        s"return coverage when ${td.name}" in new Fixture {
          verify(measure, getPath(td.cls), td.expected)
        }
      }
    }
  }
}

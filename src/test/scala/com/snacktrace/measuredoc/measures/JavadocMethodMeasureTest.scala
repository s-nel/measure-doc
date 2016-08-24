package com.snacktrace.measuredoc.measures

import com.snacktrace.measuredoc._
import com.snacktrace.measuredoc.javafiles._
import org.scalatest.WordSpec

class JavadocMethodMeasureTest extends WordSpec with BaseMeasureTestSupport {
  trait Fixture {
    val measure = new JavadocMethodMeasure()

    final case class TD(name: String, cls: Class[_], expected: Coverages)

    val testDatas = Seq(
      TD("missing method javadoc",
        classOf[MethodMissing],
        Coverages(Set(
          Coverage(
            "MethodMissing",
            Method,
            0,
            2,
            MissingCoverages(Set(
              MissingCoverage(Text),
              MissingCoverage(Param, Some("param"))
            ))
          ),
          Coverage(
            "foo",
            Method,
            0,
            3,
            MissingCoverages(Set(
              MissingCoverage(Return),
              MissingCoverage(Throws, Some("Exception")),
              MissingCoverage(Text)
            ))
          )
        ))
      ),
      TD("complete method javadoc",
        classOf[MethodComplete],
        Coverages(Set(
          Coverage(
            "foo",
            Method,
            8,
            8,
            MissingCoverages(Set())
          ),
          Coverage(
            "MethodComplete",
            Method,
            4,
            4,
            MissingCoverages(Set())
          )
        ))
      ),
      TD("partial method javadoc",
        classOf[MethodPartial],
        Coverages(Set(
          Coverage(
            "noSummary",
            Method,
            4,
            5,
            MissingCoverages(Set(
              MissingCoverage(Text)
            ))
          ),
          Coverage(
            "noParam",
            Method,
            4,
            5,
            MissingCoverages(Set(
              MissingCoverage(Param, Some("param"))
            ))
          ),
          Coverage(
            "noTypeParam",
            Method,
            4,
            5,
            MissingCoverages(Set(
              MissingCoverage(Param, Some("T"))
            ))
          ),
          Coverage(
            "noThrows",
            Method,
            4,
            5,
            MissingCoverages(Set(
              MissingCoverage(Throws, Some("IOException"))
            ))
          ),
          Coverage(
            "noReturn",
            Method,
            4,
            5,
            MissingCoverages(Set(
              MissingCoverage(Return, None)
            ))
          )
        ))
      )
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

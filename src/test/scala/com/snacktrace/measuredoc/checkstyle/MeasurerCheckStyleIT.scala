package com.snacktrace.measuredoc.checkstyle

import java.io.File

import com.snacktrace.measuredoc.javafiles.{TypeParamPartial, TypeParamMissing}
import com.snacktrace.measuredoc._
import com.snacktrace.measuredoc.measures.{JavadocMethodMeasure, JavadocTypeMeasure}
import org.scalatest.{MustMatchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class MeasurerCheckStyleIT extends WordSpec with MustMatchers with BaseMeasureTestSupport {
  trait Fixture {
    val measurer = new MeasurerCheckStyle(Seq(new JavadocTypeMeasure(), new JavadocMethodMeasure()))

    final case class TD(name: String, files: Seq[File], expected: Coverages)

    val testDatas = Seq(
      TD("multiple files", Seq(
        getPath(classOf[TypeParamMissing[_, _]]),
        getPath(classOf[TypeParamPartial[_, _]])),
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
          ),
          Coverage(
            classOf[TypeParamPartial[_, _]].getSimpleName,
            Type,
            1,
            3,
            MissingCoverages(Set(
              MissingCoverage(Text),
              MissingCoverage(Param, Some("T"))
            ))
          ),
          Coverage(
            "TypeParamPartial",
            Method,
            0,
            2,
            MissingCoverages(Set(
              MissingCoverage(Text),
              MissingCoverage(Param, Some("blah"))
            ))
          )
        ))))
  }

  new Fixture {
    "MeasurerCheckStyle.measure" should {
      testDatas.foreach { td =>
        s"return coverage when ${td.name}" in new Fixture {
          val actual = Await.result(measurer.measure(td.files), 10.seconds)
          actual mustBe td.expected
        }
      }
    }
  }
}

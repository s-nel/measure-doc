package com.snacktrace.measuredoc

import java.io.File

import com.google.checkstyle.test.base.BaseCheckTestSupport
import com.snacktrace.measuredoc.checkstyle.MeasurerCheckStyle
import org.scalatest.MustMatchers
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait BaseMeasureTestSupport extends MustMatchers {
  protected def getPath(cls: Class[_]): File = {
    return new File(s"src/test/java/com/snacktrace/measuredoc/javafiles/${cls.getSimpleName}.java")
  }

  protected def verify(measure: StatefulMeasure, file: File, expected: Coverages) = {
    val actual = Await.result(new MeasurerCheckStyle(Seq(measure)).measure(Seq(file)), 30.seconds)
    actual mustBe expected
  }
}

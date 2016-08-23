package com.snacktrace.measuredoc

import java.io.File

import scala.concurrent.Future

trait Measurer {
  def measure(files: Seq[File]): Future[Coverages]
}

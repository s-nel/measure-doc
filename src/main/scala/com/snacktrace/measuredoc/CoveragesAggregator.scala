package com.snacktrace.measuredoc

trait CoveragesAggregator {
  def aggregate(c1: Coverages, c2: Coverages): Coverages
}

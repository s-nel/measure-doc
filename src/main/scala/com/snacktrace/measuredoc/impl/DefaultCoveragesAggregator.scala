package com.snacktrace.measuredoc.impl

import com.snacktrace.measuredoc.{Coverages, CoveragesAggregator}

class DefaultCoveragesAggregator extends CoveragesAggregator {
  override def aggregate(c1: Coverages, c2: Coverages): Coverages = {
    Coverages(c1.value ++ c2.value)
  }
}

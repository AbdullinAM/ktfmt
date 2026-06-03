/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.ktfmt.benchmarks

import com.facebook.ktfmt.format.Formatter
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Param
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

/**
 * Measures the formatting time scaling with input size by formatting a synthetic file
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
open class SyntheticScalingBenchmark {

  @Param("10", "100", "1000") var units: Int = 0

  private lateinit var code: String

  @Setup
  fun generate() {
    code = buildString {
      appendLine("package com.example.generated")
      appendLine()
      for (i in 0 until units) {
        appendLine(unit(i))
        appendLine()
      }
    }
  }

  @Benchmark fun format(): String = Formatter.format(Formatter.META_FORMAT, code)

  private fun unit(i: Int): String =
      """
      |fun compute$i(input: List<Int>, factor: Int = 2): Map<String, Int>    {
      |    return input
      |      .filter { 
      |                  it % 2 == 0 }
      |      .map { 
      |      it * factor 
      |      }.associateBy { value ->
      |       "key_" + value.toString() + "_$i" 
      |}
      |      .filterValues { it > 0 }
      |}
      """
          .trimMargin()
}

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
 * Guards that formatting already-formatted code (`preformatted`, run through ktfmt to a fixed point)
 * costs about the same as formatting code that needs changes (`original`) — ktfmt re-parses and
 * re-lays-out either way. Runs across the full [RealSourcesBenchmark] fixture set; the preformatted
 * variant is derived at setup time (untimed) by formatting the original to convergence.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
open class RealSourcesBenchmark  {

    @Param(
        "coroutines/Flow.kt",
        "serialization/Json.kt",
        "arrow/Either.kt",
        "coroutines/Job.kt",
        "kotlinpoet/FunSpecTest.kt",
    )
    var fixture: String = ""

    @Param("original", "preformatted") var variant: String = ""

    private lateinit var code: String

    @Setup
    fun load() {
        val path = "/corpus/$fixture"
        val original =
            javaClass.getResourceAsStream(path)?.use { it.readBytes().toString(Charsets.UTF_8) }
                ?: error("Missing benchmark corpus resource: $path")
        // ktfmt is idempotent, so a single pass over the original yields the fixed point.
        code =
            when (variant) {
                "original" -> original
                "preformatted" -> Formatter.format(Formatter.META_FORMAT, original)
                else -> error("Unknown variant: $variant")
            }
    }

    @Benchmark fun format(): String = Formatter.format(Formatter.META_FORMAT, code)
}

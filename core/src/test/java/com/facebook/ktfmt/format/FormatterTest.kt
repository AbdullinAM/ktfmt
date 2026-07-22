package com.facebook.ktfmt.format

import com.facebook.ktfmt.FormatterTestFixture
import com.facebook.ktfmt.TEST_DATA_ROOT
import com.facebook.ktfmt.runFormatterTest
import java.nio.file.Files
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.streams.asSequence
import kotlin.test.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class FormatterTest(private val relativePath: String) {

  @Test fun format() = runFormatterTest(relativePath)

  companion object {
    val TEST_DATA_PATH = TEST_DATA_ROOT.resolve("format/")

    @JvmStatic
    @Parameterized.Parameters(name = "{0}")
    fun data(): List<Array<String>> =
        Files.walk(TEST_DATA_PATH)
            .asSequence()
            .filter { it.isRegularFile() }
            .filter { it.extension == "kt" || it.extension == "kts" }
            // skip test infra files
            .filterNot { file ->
              FormatterTestFixture.TEST_FILE_PATTERNS.any { file.name.contains(it) }
            }
            .map { TEST_DATA_ROOT.relativize(it).toString() }
            .sorted()
            .map { arrayOf(it) }
            .toList()
  }
}

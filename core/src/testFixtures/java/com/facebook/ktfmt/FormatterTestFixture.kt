package com.facebook.ktfmt

import com.facebook.ktfmt.debughelpers.PrintAstVisitor
import com.facebook.ktfmt.format.Formatter
import com.facebook.ktfmt.format.Parser
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.io.path.writeText
import org.junit.Assert

fun runFormatterTest(path: String, body: FormatterTestFixture.() -> Unit = {}) {
  val fixture = FormatterTestFixture(path)
  fixture.body()
  fixture.runTest()
}

class FormatterTestFixture(
    val sourcePath: String,
) {
  val formattingOptionsDirectives = FormattingOptionsDirectives()
  val formatterTestOptions = FormatterTestDirectives()

  val formattingOptions
    get() = formattingOptionsDirectives.result

  val testOptions
    get() = formatterTestOptions.result

  fun runTest() {
    val sourceFile = TEST_DATA_PATH.resolve(sourcePath)
    val expectedFile = sourceFile.resolveExpectedFile()

    var sourceCode = sourceFile.readText()
    sourceCode = formatterTestOptions.processDirectives(sourceCode).trim()
    sourceCode = formattingOptionsDirectives.processDirectives(sourceCode).trim()

    if (!expectedFile.exists()) {
      val formattedCode = formatCode(sourceCode)
      expectedFile.writeText(formattedCode)
      Assert.fail("Expected file $expectedFile does not exist, created code:\n$formattedCode")
    }

    val expectedFormatting = expectedFile.readText()
    checkTrailingWhitespaces(expectedFormatting)

    val actualFormatting = formatCode(sourceCode)
    if (actualFormatting != expectedFormatting) {
      val actualFile = sourceFile.resolveActualFile()
      actualFile.writeText(actualFormatting)

      Assert.fail(
          "Formatter output does not match expected code.\n" +
                  "See ${actualFile.toAbsolutePath()}.\n" +
                  buildParseTree(sourceCode)
      )
    }
    if (testOptions.checkIdempotency) {
      val secondFormatting = Formatter.format(formattingOptions, actualFormatting)
      if (actualFormatting != secondFormatting) {
        val firstReformatFile =
            sourceFile.resolveSiblingBySuffix("reformat1").also {
              it.writeText(actualFormatting)
            }
        val secondReformatFile =
            sourceFile.resolveSiblingBySuffix("reformat2").also {
              it.writeText(secondFormatting)
            }

        Assert.fail(
            "Formatter output is not idempotent.\n" +
                "See ${firstReformatFile.toAbsolutePath()} and ${secondReformatFile.toAbsolutePath()}.\n" +
                buildParseTree(sourceCode)
        )
      }
    }
  }

  private fun formatCode(code: String): String = Formatter.format(formattingOptions, code)

  private fun buildParseTree(code: String): String = buildString {
    val file = Parser.parse(code)
    appendLine("# Parse tree of input: ")
    appendLine("#".repeat(20))
    val baos = ByteArrayOutputStream()
    file.accept(PrintAstVisitor(PrintStream(baos, true, "UTF-8")))
    appendLine(baos.toString("UTF-8"))
    appendLine()
    appendLine("# Input: ")
    appendLine("#".repeat(20))
    appendLine(code)
    appendLine()
  }

  private fun checkTrailingWhitespaces(expectedFormatting: String) {
    if (
        !testOptions.allowTrailingWhitespace && expectedFormatting.lines().any { it.endsWith(" ") }
    ) {
      throw RuntimeException(
          "Expected code contains trailing whitespace, which the formatter usually doesn't output:\n" +
              expectedFormatting.lines().joinToString("\n") {
                if (it.endsWith(" ")) "[$it]" else it
              },
      )
    }
  }

  companion object {
    val TEST_DATA_PATH: Path = Paths.get("testData")

    val TEST_FILE_PATTERNS = listOf(
      ".expected.",
      ".actual.",
      ".reformat1.",
      ".reformat2.",
    )

    fun Path.resolveSiblingBySuffix(suffix: String): Path {
      val fileName = this.fileName
      val name = fileName.nameWithoutExtension
      val extension = fileName.extension
      return resolveSibling("$name.$suffix.$extension")
    }

    fun Path.resolveExpectedFile(): Path = resolveSiblingBySuffix("expected")

    fun Path.resolveActualFile(): Path = resolveSiblingBySuffix("actual")
  }
}

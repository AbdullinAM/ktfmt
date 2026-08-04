package com.facebook.ktfmt

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class GenerateFormatterTestTask : DefaultTask() {

  @get:InputDirectory abstract val testDataDir: DirectoryProperty

  @get:OutputDirectory abstract val outputDir: DirectoryProperty

  init {
    group = "build"
    description = "Generates formatter tests from test data sources"
  }

  @TaskAction
  fun generate() {
    val output = outputDir.file("FormatterTest.kt").get().asFile
    output.parentFile.mkdirs()

    output.writeText(
        buildString {
          appendLine("package com.facebook.ktfmt.format")
          appendLine("import kotlin.test.Test")
          appendLine("import com.facebook.ktfmt.runFormatterTest")
          appendLine()
          appendLine("class FormatterTest {")

          emitDirectory(
              builder = this,
              root = testDataDir.get().asFile,
              directory = testDataDir.get().asFile,
              indent = "    ",
              skipRoot = true,
          )

          appendLine("}")
        },
    )
  }

  private fun emitDirectory(
      builder: StringBuilder,
      root: File,
      directory: File,
      indent: String,
      skipRoot: Boolean,
  ) {
    if (!skipRoot) {
      builder.appendLine("${indent}class ${className(directory.name)} {")
      builder.appendLine()
    }

    val bodyIndent = if (skipRoot) indent else "$indent    "

    directory
        .listFiles()
        ?.filter(File::isFile)
        ?.sortedBy(File::getName)
        ?.filter { it.extension == "kt" || it.extension == "kts" }
        ?.filterNot { file ->
          TEST_FILE_PATTERNS.any { file.name.contains(it) }
        }
        ?.forEach { file ->
          val method = sanitize(file.nameWithoutExtension)
          val relative = file.relativeTo(root).invariantSeparatorsPath

          builder.appendLine("${bodyIndent}@Test")
          builder.appendLine("${bodyIndent}fun `$method`() {")
          builder.appendLine("${bodyIndent}    runFormatterTest(\"$relative\")")
          builder.appendLine("${bodyIndent}}")
          builder.appendLine()
        }

    directory.listFiles()?.filter(File::isDirectory)?.sortedBy(File::getName)?.forEach { child ->
      emitDirectory(
          builder,
          root,
          child,
          bodyIndent,
          skipRoot = false,
      )
    }

    if (!skipRoot) {
      builder.appendLine("${indent}}")
      builder.appendLine()
    }
  }

  private fun sanitize(name: String) = name.replace(Regex("[^A-Za-z0-9_]"), "_")

  private fun className(name: String) = sanitize(name).replaceFirstChar(Char::uppercase)

  companion object {

    val TEST_FILE_PATTERNS = listOf(
        ".expected.",
        ".actual.",
        ".reformat1.",
        ".reformat2.",
    )
  }
}

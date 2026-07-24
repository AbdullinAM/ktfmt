package com.facebook.ktfmt

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

@Suppress("unused")
class GenerateFormatterTestPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val testDataDir = project.layout.projectDirectory.dir("testData/format/")
        val outputDir = project.layout.buildDirectory.dir("generated/test/java")

        project.tasks.register<GenerateFormatterTestTask>("generateFormatterTest") {
            this.testDataDir.set(testDataDir)
            this.outputDir.set(outputDir)
        }
    }
}

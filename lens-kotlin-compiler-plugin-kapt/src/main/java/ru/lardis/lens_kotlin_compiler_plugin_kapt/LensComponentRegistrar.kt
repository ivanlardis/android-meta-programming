package ru.lardis.lens_kotlin_compiler_plugin_kapt

import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
import java.io.File

class LensComponentRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        val outputDirectory: File = configuration.get(JVMConfigurationKeys.OUTPUT_DIRECTORY)!!
        AnalysisHandlerExtension.registerExtension(project, LensAnalysisHandler(outputDirectory))
    }
}
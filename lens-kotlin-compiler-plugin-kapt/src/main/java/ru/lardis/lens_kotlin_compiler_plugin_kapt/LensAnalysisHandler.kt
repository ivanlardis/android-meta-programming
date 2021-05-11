package ru.lardis.lens_kotlin_compiler_plugin_kapt

import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.allConstructors
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
import ru.lardis.lens_kotlin_compiler_plugin_kapt.extensions.SimpleClass
import ru.lardis.lens_kotlin_compiler_plugin_kapt.extensions.SimpleClass.SimpleProperty
import ru.lardis.lens_kotlin_compiler_plugin_kapt.extensions.appendText
import ru.lardis.lens_kotlin_compiler_plugin_kapt.extensions.isLens
import ru.lardis.lens_kotlin_compiler_plugin_kapt.extensions.toLensText
import java.io.File


class LensAnalysisHandler(
    outputDirectory: File
) : AnalysisHandlerExtension {
    private val outputDirectoryPath = outputDirectory.path + "/simple-ksp"
    private var generated = false

    override fun analysisCompleted(
        project: Project,
        module: ModuleDescriptor,
        bindingTrace: BindingTrace,
        files: Collection<KtFile>
    ): AnalysisResult? {
        if (generated) {
            return null
        }

        val newFiles = files.filter { !it.virtualFilePath.startsWith(outputDirectoryPath) }
            .flatMap { it.declarations }
            .filterIsInstance(KtClass::class.java)
            .filter { it.isLens }
            .map { lens ->
                val properties: List<SimpleProperty> =
                    lens.allConstructors.firstOrNull()
                        ?.valueParameters
                        ?.map {
                            SimpleProperty(
                                it.name!!,
                                it.typeReference?.text!!
                            )
                        }.orEmpty()

                val lensClass = SimpleClass(
                    lens.name!!,
                    lens.containingKtFile.packageName,
                    properties
                )

                File(outputDirectoryPath).mkdir()
                val lensFile = File(outputDirectoryPath, "${lensClass.simpleName}.kt")

                lensFile.createNewFile()
                lensFile appendText lensClass.toLensText()
                lensFile
            }

        generated = true

        return when {
            newFiles.isEmpty() -> null
            else -> {
                AnalysisResult.RetryWithAdditionalRoots(
                    bindingContext = bindingTrace.bindingContext,
                    moduleDescriptor = module,
                    additionalKotlinRoots = newFiles,
                    additionalJavaRoots = emptyList(),
                    addToEnvironment = true
                )
            }
        }
    }


}
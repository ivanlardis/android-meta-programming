package ru.lardis.lens_apt_compiler

import ru.lardis.lens_core.Optics
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.FileObject
import javax.tools.StandardLocation

@SupportedAnnotationTypes("ru.lardis.lens_core.Optics")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class LensAptProcessor : AbstractProcessor() {

    override fun process(
            typeElements: MutableSet<out TypeElement>,
            roundEnvironment: RoundEnvironment,
    ): Boolean {
        if (roundEnvironment.processingOver()) return true

        roundEnvironment.getElementsAnnotatedWith(Optics::class.java)
                .forEach { lens ->

                    val properties = lens.enclosedElements
                            .filter(::isDataClassField)
                            .map { field ->
                                SimpleClass.SimpleProperty(
                                        field.simpleName,
                                        field.kotlinType
                                )
                            }

                    val lensClass = SimpleClass(
                            lens.className.simpleName,
                            lens.className.packageName,
                            properties
                    )

                    val lensFile: FileObject = processingEnv.filer.createResource(
                            StandardLocation.SOURCE_OUTPUT,
                            lensClass.packageName,
                            "${lensClass.simpleName}Lens.kt",
                            lens
                    )

                    lensFile.appendText(lensClass.toLensText())
                }

        return true
    }
}



package ru.lardis.ksp_compiler

import SimpleClass
import appendText
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import isKSClassDeclaration
import ru.lardis.lens_core.Optics
import toLensText
import typeQualifiedName
import java.io.OutputStream

class LensKspProcessor(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val lensList: Sequence<KSAnnotated> =
            resolver.getSymbolsWithAnnotation(Optics::class.java.name)

        lensList
            .filterIsInstance(KSClassDeclaration::class.java)
            .forEach { classDeclaration ->

                val properties = classDeclaration.getAllProperties()
                    .map {
                        SimpleClass.SimpleProperty(
                            it.simpleName.asString(),
                            it.typeQualifiedName
                        )
                    }

                val lensClass = SimpleClass(
                    classDeclaration.simpleName.asString(),
                    classDeclaration.containingFile!!.packageName.asString(),
                    properties.toList()
                )

                val file: OutputStream = codeGenerator.createNewFile(
                    Dependencies(aggregating = false, classDeclaration.containingFile!!),
                    lensClass.packageName,
                    "${lensClass.simpleName}Lens"
                )
                file appendText lensClass.toLensText()
                file.close()
            }

        return lensList.filter { !it.isKSClassDeclaration() }.toList()
    }
}
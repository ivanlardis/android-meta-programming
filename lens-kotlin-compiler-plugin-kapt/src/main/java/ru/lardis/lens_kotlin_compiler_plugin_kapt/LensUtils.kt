package ru.lardis.lens_kotlin_compiler_plugin_kapt.extensions

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import java.io.File


data class SimpleClass(
    val simpleName: String,
    val packageName: String,
    val properties: List<SimpleProperty>,
) {
    val classQualifiedName
        get() = "$packageName.$simpleName"

    data class SimpleProperty(
        val name: String,
        val type: String,
    )
}

fun SimpleClass.toLensText(): String {
    val lensImport = """
            // Generated by kotlin compiler plugin kapt
            package $packageName
            
            import ru.lardis.lens_core.Lens
            import ru.lardis.lens_core.andThen
             
           
        """.trimIndent()

    val lensProperties = properties.joinToString(
        separator = "\n\n",
        transform = { (propertyName, propertyTypeName) ->
            propertyLensText(simpleName, classQualifiedName, propertyName, propertyTypeName)
        }
    )

    return lensImport + lensProperties
}

private fun propertyLensText(
    simpleName: String,
    classQualifiedName: String,
    propertyName: String,
    propertyTypeName: String,
): String = """
            val <A> Lens<A, ${classQualifiedName}>.$propertyName: Lens<A, $propertyTypeName>
                get() = this andThen ${classQualifiedName}.$propertyName
            
            inline val ${classQualifiedName}.Companion.$propertyName: Lens<${classQualifiedName}, $propertyTypeName>
                inline get() = Lens(
                        get = { ${simpleName}: ${classQualifiedName} -> ${simpleName}.$propertyName },
                        set = { ${simpleName}: ${classQualifiedName}, value: $propertyTypeName ->
                            ${simpleName}.copy($propertyName = value)
                        }
                )
        """.trimIndent()

infix fun File.appendText(str: String) {
    val lensOutputStream = outputStream()
    lensOutputStream.write(str.toByteArray())
    lensOutputStream.close()
}

val KtClass.isLens
    get() = annotationEntries.any {
        it.shortName == FqName("ru.lardis.lens_core.Optics").shortName()
    }






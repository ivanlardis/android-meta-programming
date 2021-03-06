package ru.lardis.lens_apt_compiler

import com.squareup.kotlinpoet.ClassName
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Name
import javax.tools.FileObject

data class SimpleClass(
        val simpleName: String,
        val packageName: String,
        val properties: List<SimpleProperty>,
) {
    val classQualifiedName
        get() = "$packageName.$simpleName"

    data class SimpleProperty(
            val name: Name,
            val type: String,
    )
}

fun SimpleClass.toLensText(): String {
    val lensImport = """
            // Generated by kapt
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
        propertyName: Name,
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

fun FileObject.appendText(str: String) {
    val outputStream = openOutputStream()
    outputStream.write(str.toByteArray())
    outputStream.close()
}

val Element.className: ClassName
    get() = ClassName.bestGuess(this.toString())

fun isDataClassField(it: Element) = it.kind == ElementKind.FIELD && it.simpleName.toString() != "Companion"

//TODO ??????????????
val Element.kotlinType: String
    get() = asType().toString().removePrefix("java.lang.")
            .replace("int", "Int")
            .replace("double", "Double")
            .replace("float", "Float")



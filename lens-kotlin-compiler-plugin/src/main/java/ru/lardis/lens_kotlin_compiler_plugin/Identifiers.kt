package ru.lardis.lens_kotlin_compiler_plugin

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal object Names {
    val DEFAULT_COMPANION = Name.identifier("Companion")

    val LENS_TEST_METHOD = Name.identifier("myStringQwer")
}

internal object FqNames {
    val OPTICS_ANNOTATION = FqName("ru.lardis.lens_core.Optics")
}

internal object ClassIds {
    val LENS = ClassId(FqName("ru.lardis.lens_core"), Name.identifier("Lens"))
    val LENS_COMPANION = ClassId(FqName("ru.lardis.lens_core.Lens"), Name.identifier("Companion"))
    val K_FUNCTION1 = ClassId(FqName("kotlin.reflect"), Name.identifier("KFunction1"))
    val K_FUNCTION2 = ClassId(FqName("kotlin.reflect"), Name.identifier("KFunction2"))
    val AND_THEN = ClassId(FqName("ru.lardis.lens_core"), Name.identifier("andThen"))
}
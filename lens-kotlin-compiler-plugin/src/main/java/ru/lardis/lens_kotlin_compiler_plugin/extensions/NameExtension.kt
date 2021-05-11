package ru.lardis.lens_kotlin_compiler_plugin.extensions

import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.capitalizeDecapitalize.decapitalizeSmart

fun Name.decapitalize() = Name.identifier(identifier.decapitalizeSmart())



package ru.lardis.lens_kotlin_compiler_plugin

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import ru.lardis.lens_kotlin_compiler_plugin.extensions.dataClassParameters
import ru.lardis.lens_kotlin_compiler_plugin.extensions.getOpticsForCompanion
import java.util.*

internal class LensResolveExtension : SyntheticResolveExtension {

    override fun getSyntheticCompanionObjectNameIfNeeded(thisDescriptor: ClassDescriptor): Name =
        Names.DEFAULT_COMPANION

    override fun getSyntheticPropertiesNames(thisDescriptor: ClassDescriptor): List<Name> =
        thisDescriptor.getOpticsForCompanion()
            ?.dataClassParameters
            ?.map { it.name }
            ?: emptyList()

    override fun generateSyntheticProperties(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: ArrayList<PropertyDescriptor>,
        result: MutableSet<PropertyDescriptor>
    ) {
        val lensClassDescriptor = thisDescriptor.getOpticsForCompanion() ?: return
        createLensDescriptor(thisDescriptor, lensClassDescriptor, name)?.let(result::add)
        createLensComposeDescriptor(thisDescriptor, lensClassDescriptor, name)?.let(result::add)
    }

    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor) = thisDescriptor.getOpticsForCompanion()
        ?.dataClassParameters
        ?.flatMap {
            listOf(
                Name.identifier("${it.name.asString()}Get"),
                Name.identifier("${it.name.asString()}Set")
            ) }
        ?: emptyList()

    override fun generateSyntheticMethods(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: List<SimpleFunctionDescriptor>,
        result: MutableCollection<SimpleFunctionDescriptor>
    ) {
        val lensClassDescriptor = thisDescriptor.getOpticsForCompanion() ?: return
        createLensGetDescriptor(thisDescriptor, lensClassDescriptor, name)?.let(result::add)
        createLensSetDescriptor(thisDescriptor, lensClassDescriptor, name)?.let(result::add)
    }
}

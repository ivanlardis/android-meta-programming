package ru.lardis.lens_kotlin_compiler_plugin

import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class LensLoweringExtension : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        LensLoweringPass(pluginContext).lower(moduleFragment)
    }
}

private class LensLoweringPass(private val pluginContext: IrPluginContext) : ClassLoweringPass {

    override fun lower(irClass: IrClass) {
        LensLoweringGenerator(pluginContext).addLensIfNeeded(irClass)
    }
}

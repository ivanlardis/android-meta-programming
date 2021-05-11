package ru.lardis.lens_kotlin_compiler_plugin.extensions

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.functions
import ru.lardis.lens_kotlin_compiler_plugin.ClassIds

fun IrPluginContext.blockBody(
    symbol: IrSymbol,
    block: IrBlockBodyBuilder.() -> Unit
): IrBlockBody = DeclarationIrBuilder(this, symbol).irBlockBody { block() }

val IrPluginContext.lensClassSymbol: IrClassSymbol
    get() = referenceClass(ClassIds.LENS_COMPANION.asSingleFqName())
        ?: throw IllegalStateException("Lens core is not available")

val IrPluginContext.function1ClassSymbol: IrClassSymbol
    get() = referenceClass(ClassIds.K_FUNCTION1.asSingleFqName())
        ?: throw IllegalStateException("KFunction1 is not available")

val IrPluginContext.function2ClassSymbol: IrClassSymbol
    get() = referenceClass(ClassIds.K_FUNCTION2.asSingleFqName())
        ?: throw IllegalStateException("KFunction2 is not available")

val IrPluginContext.andThenFunctionSymbol: IrSimpleFunctionSymbol
    get() = referenceFunctions(ClassIds.AND_THEN.asSingleFqName())
        .firstOrNull()
        ?: throw IllegalStateException("andThen is not available")

val IrPluginContext.lensInvokeFunction: IrSimpleFunction
    get() = lensClassSymbol.owner.functions.first { it.name.asString() == "invoke" }

package ru.lardis.lens_kotlin_compiler_plugin

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionReferenceImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.*
import ru.lardis.lens_kotlin_compiler_plugin.extensions.*

class LensLoweringGenerator(
        private val pluginContext: IrPluginContext
) {

    fun addLensIfNeeded(irClass: IrClass) {
        if (irClass.hasAnnotation(FqNames.OPTICS_ANNOTATION)) {
            irClass.addLensGet()
            irClass.addLensSet()
            irClass.addLens()
            irClass.addComposeLens()
        }
    }

    /**
    val myString
    get() = Lens(::myStringGet, ::myStringSet)
    _________________________________________________________________
    PROPERTY name:myString visibility:public modality:FINAL [val]
    FUN name:<get-myString> visibility:public modality:FINAL <> ($this:ru.lardis.meta.model.C.Companion) returnType:ru.lardis.lens_core.Lens<ru.lardis.meta.model.C, kotlin.String>
    correspondingProperty: PROPERTY name:myString visibility:public modality:FINAL [val]
    $this: VALUE_PARAMETER name:<this> type:ru.lardis.meta.model.C.Companion
    BLOCK_BODY
    RETURN type=kotlin.Nothing from='public final fun <get-myString> (): ru.lardis.lens_core.Lens<ru.lardis.meta.model.C, kotlin.String> declared in ru.lardis.meta.model.C.Companion'
    CALL 'public final fun invoke <A, B> (get: kotlin.Function1<A of ru.lardis.lens_core.Lens.Companion.invoke, B of ru.lardis.lens_core.Lens.Companion.invoke>, set: kotlin.Function2<A of ru.lardis.lens_core.Lens.Companion.invoke, B of ru.lardis.lens_core.Lens.Companion.invoke, A of ru.lardis.lens_core.Lens.Companion.invoke>): ru.lardis.lens_core.Lens<A of ru.lardis.lens_core.Lens.Companion.invoke, B of ru.lardis.lens_core.Lens.Companion.invoke> [operator] declared in ru.lardis.lens_core.Lens.Companion' type=ru.lardis.lens_core.Lens<ru.lardis.meta.model.C, kotlin.String> origin=INVOKE
    <A>: ru.lardis.meta.model.C
    <B>: kotlin.String
    $this: GET_OBJECT 'CLASS IR_EXTERNAL_DECLARATION_STUB OBJECT name:Companion modality:FINAL visibility:public [companion] superTypes:[kotlin.Any]' type=ru.lardis.lens_core.Lens.Companion
    get: FUNCTION_REFERENCE 'private final fun myStringGet (c: ru.lardis.meta.model.C): kotlin.String declared in ru.lardis.meta.model.C.Companion' type=kotlin.reflect.KFunction1<ru.lardis.meta.model.C, kotlin.String> origin=null reflectionTarget=<same>
    $this: GET_VAR '<this>: ru.lardis.meta.model.C.Companion declared in ru.lardis.meta.model.C.Companion.<get-myString>' type=ru.lardis.meta.model.C.Companion origin=null
    set: FUNCTION_REFERENCE 'private final fun myStringSet (c: ru.lardis.meta.model.C, value: kotlin.String): ru.lardis.meta.model.C declared in ru.lardis.meta.model.C.Companion' type=kotlin.reflect.KFunction2<ru.lardis.meta.model.C, kotlin.String, ru.lardis.meta.model.C> origin=null reflectionTarget=<same>
    $this: GET_VAR '<this>: ru.lardis.meta.model.C.Companion declared in ru.lardis.meta.model.C.Companion.<get-myString>' type=ru.lardis.meta.model.C.Companion origin=null

     */
    private fun IrClass.addLens() {
        for (property: IrProperty in companionObject()!!.properties) {

            val lensGetFunction: IrSimpleFunction = companionObject()!!.functions.firstOrNull {
                it.name.asString() == "${property.name.asString()}Get"
            } ?: continue

            val lensSetFunction: IrSimpleFunction = companionObject()!!.functions.firstOrNull {
                it.name.asString() == "${property.name.asString()}Set"
            } ?: continue

            if (property.getter!!.extensionReceiverParameter != null) {
                continue
            }

            val lensProperty: IrProperty? = properties
                    .firstOrNull { it.name.asString() == property.name.asString() }

            val lensFunction: IrSimpleFunction = lensProperty?.getter ?: continue

            val blockBody = pluginContext.blockBody(property.getter!!.symbol) {
                +irReturn(
                        irCall(
                                pluginContext.lensInvokeFunction.symbol,
                                property.getter!!.returnType
                        ).also {
                            it.dispatchReceiver = irGetObject(pluginContext.lensClassSymbol)

                            it.putTypeArgument(0, defaultType)
                            it.putTypeArgument(1, lensFunction.returnType)

                            val lensGetFunctionType: IrType = pluginContext.function1ClassSymbol.typeWith(
                                    listOf(
                                            defaultType,
                                            lensFunction.returnType
                                    )
                            )

                            val lensSetFunctionType: IrType = pluginContext.function2ClassSymbol.typeWith(
                                    listOf(
                                            defaultType,
                                            lensFunction.returnType,
                                            defaultType,
                                    )
                            )

                            val lensGetFunctionReference = IrFunctionReferenceImpl(
                                    startOffset, endOffset, lensGetFunctionType,
                                    lensGetFunction.symbol,
                                    typeArgumentsCount = 0,
                                    valueArgumentsCount = lensGetFunction.valueParameters.size,
                                    reflectionTarget = null,
                            )


                            lensGetFunctionReference.dispatchReceiver =
                                    irGet(property.getter!!.dispatchReceiverParameter!!)

                            val lensSetFunctionReference = IrFunctionReferenceImpl(
                                    startOffset, endOffset, lensSetFunctionType,
                                    lensSetFunction.symbol,
                                    typeArgumentsCount = 0,
                                    valueArgumentsCount = lensSetFunction.valueParameters.size,
                                    reflectionTarget = null,
                            )

                            lensSetFunctionReference.dispatchReceiver =
                                    irGet(property.getter!!.dispatchReceiverParameter!!)

                            it.putValueArgument(0, lensGetFunctionReference)
                            it.putValueArgument(1, lensSetFunctionReference)
                        }
                )
            }

            property.getter!!.body = blockBody
        }
    }

    /**
    private fun myStringGet(c: C): String {
    return c.myString
    }
    _________________________________________________________________
    FUN name:myStringGet visibility:private modality:FINAL <> ($this:ru.lardis.meta.model.C.Companion, c:ru.lardis.meta.model.C) returnType:kotlin.String
    $this: VALUE_PARAMETER name:<this> type:ru.lardis.meta.model.C.Companion
    VALUE_PARAMETER name:c index:0 type:ru.lardis.meta.model.C
    BLOCK_BODY
    RETURN type=kotlin.Nothing from='private final fun myStringGet (c: ru.lardis.meta.model.C)
    : kotlin.String declared in ru.lardis.meta.model.C.Companion'
    CALL 'public final fun <get-myString> ()
    : kotlin.String declared in ru.lardis.meta.model.C' type=kotlin.String origin=GET_PROPERTY
    $this: GET_VAR 'c: ru.lardis.meta.model.C declared in ru.lardis.meta.model.C.Companion.myStringGet'
    type=ru.lardis.meta.model.C origin=null

     */
    private fun IrClass.addLensGet() {
        for (simpleFunction: IrSimpleFunction in companionObject()!!.functions) {

            val lensProperty: IrProperty = properties
                    .firstOrNull { "${it.name.asString()}Get" == simpleFunction.name.asString() }
                    ?: continue

            val blockBody = pluginContext.blockBody(simpleFunction.symbol) {
                +irReturn(
                        irCall(
                                lensProperty.getter!!.symbol,
                                simpleFunction.returnType
                        ).also {
                            it.dispatchReceiver = irGet(simpleFunction.valueParameters.first())
                        }
                )
            }
            simpleFunction.body = blockBody
        }
    }

    /**
    private fun myStringSet(c: C, value: String): C {
    return c.copy(myString = value)
    }
    _________________________________________________________________
    FUN name:myStringSet visibility:private modality:FINAL <> ($this:ru.lardis.meta.model.C.Companion, c:ru.lardis.meta.model.C, value:kotlin.String) returnType:ru.lardis.meta.model.C
    $this: VALUE_PARAMETER name:<this> type:ru.lardis.meta.model.C.Companion
    VALUE_PARAMETER name:c index:0 type:ru.lardis.meta.model.C
    VALUE_PARAMETER name:value index:1 type:kotlin.String
    BLOCK_BODY
    RETURN type=kotlin.Nothing from='private final fun myStringSet (c: ru.lardis.meta.model.C, value: kotlin.String): ru.lardis.meta.model.C declared in ru.lardis.meta.model.C.Companion'
    CALL 'public final fun copy (myString: kotlin.String): ru.lardis.meta.model.C declared in ru.lardis.meta.model.C' type=ru.lardis.meta.model.C origin=null
    $this: GET_VAR 'c: ru.lardis.meta.model.C declared in ru.lardis.meta.model.C.Companion.myStringSet' type=ru.lardis.meta.model.C origin=null
    myString: GET_VAR 'value: kotlin.String declared in ru.lardis.meta.model.C.Companion.myStringSet' type=kotlin.String origin=null

     */
    private fun IrClass.addLensSet() {
        for (simpleFunction: IrSimpleFunction in companionObject()!!.functions) {

            val lensProperty: IrProperty = properties
                    .firstOrNull { "${it.name.asString()}Set" == simpleFunction.name.asString() }
                    ?: continue

            val copyFunction: IrSimpleFunction = functions
                    .firstOrNull { it.name.asString() == "copy" } ?: continue

            val blockBody = pluginContext.blockBody(simpleFunction.symbol) {
                +irReturn(
                        irCall(
                                copyFunction.symbol,
                                simpleFunction.returnType
                        ).also {
                            it.dispatchReceiver = irGet(simpleFunction.valueParameters.first())
                            it.putValueArgument(
                                    properties.indexOf(lensProperty),
                                    irGet(simpleFunction.valueParameters[1]))
                        }
                )
            }
            simpleFunction.body = blockBody
        }
    }

    /**
    val <A> Lens<A, C>.myStsTest: Lens<A, String>
    get() = this andThen C.myStsTest
    _________________________________________________________________
    PROPERTY name:myStsTest visibility:public modality:FINAL [val]
    FUN name:<get-myStsTest> visibility:public modality:FINAL <A> ($this:ru.lardis.meta.model.C.Companion, $receiver:ru.lardis.lens_core.Lens<A of ru.lardis.meta.model.C.Companion.<get-myStsTest>, ru.lardis.meta.model.C>) returnType:ru.lardis.lens_core.Lens<A of ru.lardis.meta.model.C.Companion.<get-myStsTest>, kotlin.String>
    correspondingProperty: PROPERTY name:myStsTest visibility:public modality:FINAL [val]
    TYPE_PARAMETER name:A index:0 variance: superTypes:[kotlin.Any?]
    $this: VALUE_PARAMETER name:<this> type:ru.lardis.meta.model.C.Companion
    $receiver: VALUE_PARAMETER name:<this> type:ru.lardis.lens_core.Lens<A of ru.lardis.meta.model.C.Companion.<get-myStsTest>, ru.lardis.meta.model.C>
    BLOCK_BODY
    RETURN type=kotlin.Nothing from='public final fun <get-myStsTest> <A> (): ru.lardis.lens_core.Lens<A of ru.lardis.meta.model.C.Companion.<get-myStsTest>, kotlin.String> declared in ru.lardis.meta.model.C.Companion'
    CALL 'public final fun andThen <A, B, C> (other: ru.lardis.lens_core.Lens<B of ru.lardis.lens_core.LensCoreKt.andThen, C of ru.lardis.lens_core.LensCoreKt.andThen>): ru.lardis.lens_core.Lens<A of ru.lardis.lens_core.LensCoreKt.andThen, C of ru.lardis.lens_core.LensCoreKt.andThen> [infix] declared in ru.lardis.lens_core.LensCoreKt' type=ru.lardis.lens_core.Lens<A of ru.lardis.meta.model.C.Companion.<get-myStsTest>, kotlin.String> origin=null
    <A>: A of ru.lardis.meta.model.C.Companion.<get-myStsTest>
    <B>: ru.lardis.meta.model.C
    <C>: kotlin.String
    $receiver: GET_VAR '<this>: ru.lardis.lens_core.Lens<A of ru.lardis.meta.model.C.Companion.<get-myStsTest>, ru.lardis.meta.model.C> declared in ru.lardis.meta.model.C.Companion.<get-myStsTest>' type=ru.lardis.lens_core.Lens<A of ru.lardis.meta.model.C.Companion.<get-myStsTest>, ru.lardis.meta.model.C> origin=null
    other: CALL 'public final fun <get-mySts> (): ru.lardis.lens_core.Lens<ru.lardis.meta.model.C, kotlin.String> declared in ru.lardis.meta.model.C.Companion' type=ru.lardis.lens_core.Lens<ru.lardis.meta.model.C, kotlin.String> origin=GET_PROPERTY
    $this: GET_OBJECT 'CLASS OBJECT name:Companion modality:FINAL visibility:public [companion] superTypes:[kotlin.Any]' type=ru.lardis.meta.model.C.Companion

     */

    private fun IrClass.addComposeLens() {
        for (composeProperty: IrProperty in companionObject()!!.properties) {

            val lensProperty: IrProperty = properties
                    .firstOrNull { it.name.asString() == composeProperty.name.asString() }
                    ?: continue

            val lensCompanionProperty: IrProperty = companionObject()?.properties
                    ?.firstOrNull { it.name.asString() == composeProperty.name.asString() }
                    ?: continue

            if (composeProperty.getter!!.extensionReceiverParameter == null) continue

            val andThenFunctionSymbol = pluginContext.andThenFunctionSymbol

            val composePropertyType: IrType = composeProperty.getter!!.typeParameters.first().defaultType

            val extensionReceiverParameter = composeProperty.getter!!.extensionReceiverParameter!!

            val blockBody = pluginContext.blockBody(composeProperty.getter!!.symbol) {
                +irReturn(
                        irCall(
                                andThenFunctionSymbol,
                                composeProperty.getter!!.returnType
                        ).apply {
                            putTypeArgument(0, composePropertyType)
                            putTypeArgument(1, this@addComposeLens.defaultType)
                            putTypeArgument(2, lensProperty.getter!!.returnType)

                            extensionReceiver = irGet(
                                    extensionReceiverParameter.type,
                                    extensionReceiverParameter.symbol
                            )

                            putValueArgument(
                                    0,
                                    irCall(lensCompanionProperty.getter!!).apply {
                                        dispatchReceiver = irGetObject(companionObject()!!.symbol)
                                    }
                            )
                        }
                )
            }
            composeProperty.getter?.body = blockBody

        }
    }
}
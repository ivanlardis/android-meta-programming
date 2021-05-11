package ru.lardis.lens_kotlin_compiler_plugin

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.DescriptorFactory
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.SimpleType
import org.jetbrains.kotlin.types.TypeProjectionImpl
import org.jetbrains.kotlin.types.Variance
import ru.lardis.lens_kotlin_compiler_plugin.extensions.dataClassParameters
import ru.lardis.lens_kotlin_compiler_plugin.extensions.decapitalize
import ru.lardis.lens_kotlin_compiler_plugin.extensions.lensClass

/**
val myString
get() = Lens(::myStringGet, ::myStringSet)
_________________________________________________________________
PROPERTY name:myString visibility:public modality:FINAL [val]
FUN name:<get-myString> visibility:public modality:FINAL <> ($this:ru.lardis.meta.model.C.Companion) returnType:ru.lardis.lens_core.Lens<ru.lardis.meta.model.C, kotlin.String>
correspondingProperty: PROPERTY name:myString visibility:public modality:FINAL [val]
$this: VALUE_PARAMETER name:<this> type:ru.lardis.meta.model.C.Companion
 */
internal fun createLensDescriptor(
        opticsCompanionDescriptor: ClassDescriptor,
        opticsDescriptor: ClassDescriptor,
        name: Name,
): PropertyDescriptor? {

    val opticsParameter: ValueParameterDescriptor = opticsDescriptor.dataClassParameters
            .firstOrNull { it.name == name } ?: return null

    val lensObjectType: SimpleType = KotlinTypeFactory.simpleNotNullType(
            Annotations.EMPTY,
            opticsDescriptor,
            listOf()
    )

    val returnType: SimpleType = KotlinTypeFactory.simpleNotNullType(
            Annotations.EMPTY,
            opticsDescriptor.lensClass,
            listOf(
                    TypeProjectionImpl(lensObjectType), TypeProjectionImpl(opticsParameter.type)
            )
    )

    val lensProperty: PropertyDescriptorImpl = PropertyDescriptorImpl.create(
            opticsCompanionDescriptor,
            Annotations.EMPTY,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC,
            false,
            name,
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            opticsCompanionDescriptor.source,
            false,
            false,
            false,
            false,
            false,
            false,
    ).apply {
        setType(
                returnType,
                listOf(),
                opticsCompanionDescriptor.thisAsReceiverParameter,
                null
        )
    }

    val lensPropertyGetter = PropertyGetterDescriptorImpl(
            lensProperty,
            Annotations.EMPTY,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC,
            false,
            false,
            false,
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            null,
            opticsCompanionDescriptor.source
    ).apply {
        initialize(returnType)
    }

    lensProperty.initialize(lensPropertyGetter, null)

    return lensProperty
}

/**
private fun myStringGet(c: C): String {
return c.myString
}
_________________________________________________________________
FUN name:myStringGet visibility:private modality:FINAL <>
($this:ru.lardis.meta.model.C.Companion, c:ru.lardis.meta.model.C)
returnType:kotlin.String
$this: VALUE_PARAMETER name:<this> type:ru.lardis.meta.model.C.Companion
VALUE_PARAMETER name:c index:0 type:ru.lardis.meta.model.C
 */

internal fun createLensGetDescriptor(
        opticsCompanionDescriptor: ClassDescriptor,
        opticsDescriptor: ClassDescriptor,
        name: Name,
): SimpleFunctionDescriptor? {

    val opticsParameter: ValueParameterDescriptor = opticsDescriptor.dataClassParameters
            .firstOrNull {
                "${it.name.asString()}Get" == name.asString()
            } ?: return null

    return SimpleFunctionDescriptorImpl.create(
            opticsCompanionDescriptor,
            Annotations.EMPTY,
            name,
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            opticsCompanionDescriptor.source,
    ).apply {
        val valueParameter = ValueParameterDescriptorImpl(
                opticsDescriptor.constructors.first(),
                null,
                0,
                Annotations.EMPTY,
                opticsDescriptor.name.decapitalize(),
                opticsDescriptor.defaultType,
                false,
                false,
                false,
                null,
                opticsCompanionDescriptor.source,
        )
        initialize(
                null,
                opticsCompanionDescriptor.thisAsReceiverParameter,
                listOf(),
                listOf(valueParameter),
                opticsParameter.type,
                Modality.FINAL,
                DescriptorVisibilities.PRIVATE
        )
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

internal fun createLensSetDescriptor(
        opticsCompanionDescriptor: ClassDescriptor,
        opticsDescriptor: ClassDescriptor,
        name: Name,
): SimpleFunctionDescriptor? {

    val opticsParameter: ValueParameterDescriptor = opticsDescriptor.dataClassParameters
            .firstOrNull {
                "${it.name.asString()}Set" == name.asString()
            } ?: return null

    return SimpleFunctionDescriptorImpl.create(
            opticsCompanionDescriptor,
            Annotations.EMPTY,
            name,
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            opticsCompanionDescriptor.source,
    ).apply {
        val cParameter = ValueParameterDescriptorImpl(
                opticsDescriptor.constructors.first(),
                original = null,
                index = 0,
                annotations = Annotations.EMPTY,
                name = opticsDescriptor.name.decapitalize(),
                outType = opticsDescriptor.defaultType,
                declaresDefaultValue = false,
                isCrossinline = false,
                isNoinline = false,
                varargElementType = null,
                source = opticsCompanionDescriptor.source,
        )

        val valueParameter = ValueParameterDescriptorImpl(
                opticsDescriptor.constructors.first(),
                null,
                1,
                Annotations.EMPTY,
                opticsParameter.name.decapitalize(),
                opticsParameter.type,
                declaresDefaultValue = false,
                isCrossinline = false,
                isNoinline = false,
                varargElementType = null,
                source = opticsParameter.source,
        )
        initialize(
                null,
                opticsCompanionDescriptor.thisAsReceiverParameter,
                listOf(),
                listOf(cParameter, valueParameter),
                opticsDescriptor.defaultType,
                Modality.FINAL,
                DescriptorVisibilities.PRIVATE
        )
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
 */

internal fun createLensComposeDescriptor(
        opticsCompanionDescriptor: ClassDescriptor,
        opticsDescriptor: ClassDescriptor,
        name: Name,
): PropertyDescriptor? {

    val opticsParameter: ValueParameterDescriptor = opticsDescriptor.dataClassParameters
            .firstOrNull { it.name == name } ?: return null

    val lensProperty: PropertyDescriptorImpl = PropertyDescriptorImpl.create(
            opticsCompanionDescriptor,
            Annotations.EMPTY,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC,
            false,
            name,
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            opticsCompanionDescriptor.source,
            false,
            false,
            false,
            false,
            false,
            false,
    )

    val lensPropertyGetter = PropertyGetterDescriptorImpl(
            lensProperty,
            Annotations.EMPTY,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC,
            false,
            false,
            false,
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            null,
            opticsCompanionDescriptor.source
    )

    lensProperty.initialize(lensPropertyGetter, null)

    val aDescriptor: TypeParameterDescriptor = TypeParameterDescriptorImpl.createWithDefaultBound(
            lensProperty,
            Annotations.EMPTY,
            false,
            Variance.INVARIANT,
            Name.identifier("A"),
            0,
            LockBasedStorageManager.NO_LOCKS
    )

    val lensClass = opticsDescriptor.lensClass
    val returnType: SimpleType = KotlinTypeFactory.simpleNotNullType(
            Annotations.EMPTY,
            lensClass,
            listOf(
                    TypeProjectionImpl(aDescriptor.defaultType),
                    TypeProjectionImpl(opticsParameter.type)
            )
    )

    val receiverType: SimpleType = KotlinTypeFactory.simpleNotNullType(
            Annotations.EMPTY,
            lensClass,
            listOf(
                    TypeProjectionImpl(aDescriptor.defaultType),
                    TypeProjectionImpl(opticsDescriptor.defaultType)
            )
    )

    val extensionReceiverParameter = DescriptorFactory.createExtensionReceiverParameterForCallable(
            lensClass.thisAsReceiverParameter,
            receiverType,
            Annotations.EMPTY
    )

    lensProperty.setType(
            returnType,
            listOf(aDescriptor),
            opticsCompanionDescriptor.thisAsReceiverParameter,
            extensionReceiverParameter

    )

    lensPropertyGetter.initialize(returnType)

    return lensProperty
}

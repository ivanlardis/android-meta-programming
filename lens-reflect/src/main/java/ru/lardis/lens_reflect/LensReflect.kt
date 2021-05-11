package ru.lardis.lens_reflect

import ru.lardis.lens_core.Lens
import ru.lardis.lens_core.andThen
import kotlin.reflect.KCallable
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.instanceParameter

inline fun <reified A : Any, reified B> lens(property: KProperty1<A, B>): Lens<A, B> {
    return Lens(
        get = property,
        set = { a, value ->
            val copyFunction: KCallable<A> = a.copyFunction

            val args = mapOf(
                copyFunction.instanceParameter!! to a,
                copyFunction.findParameterByName(property.name)!! to value
            )

            copyFunction.callBy(args)
        }
    )
}

inline fun <reified A, reified B : Any, reified C> Lens<A, B>.at(property: KProperty1<B, C>) =
    this andThen lens(property)

inline val <reified A : Any> A.copyFunction: KCallable<A>
    get() = this::class.members.first { it.name == "copy" } as KCallable<A>

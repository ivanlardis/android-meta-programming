package ru.lardis.lens_core

interface Lens<A, B> {
    fun get(a: A): B
    fun set(a: A, b: B): A

    companion object {
        operator fun <A, B> invoke(get: (A) -> B, set: (A, B) -> A) = object : Lens<A, B> {
            override fun get(a: A): B = get(a)
            override fun set(a: A, b: B): A = set(a, b)
        }
    }
}

infix fun <A, B, C> Lens<A, B>.andThen(other: Lens<B, C>): Lens<A, C> =
    Lens(
        get = { a -> a.let(::get).let(other::get) },
        set = { a: A, c: C -> set(a, other.set(get(a), c)) }
    )
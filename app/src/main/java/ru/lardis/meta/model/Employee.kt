package ru.lardis.meta.model

import ru.lardis.lens_core.Optics

@Optics
data class Street(val number: Int, val name: String){
    companion object
}
@Optics
data class Address(val city: String, val street: Street){
    companion object
}
@Optics
data class Company(val name: String, val address: Address){
    companion object
}

@Optics
data class Employee(val name: String, val company: Company){
    companion object
}

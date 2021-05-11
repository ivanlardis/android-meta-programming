//package ru.lardis.meta
//
//import android.util.Log
//import ru.lardis.lens_core.Lens
//import ru.lardis.meta.model.Address
//import ru.lardis.meta.model.Address.Companion.street
//import ru.lardis.meta.model.Company
//import ru.lardis.meta.model.Company.Companion.address
//import ru.lardis.meta.model.Employee
//import ru.lardis.meta.model.Street
//import ru.lardis.meta.model.Street.Companion.name
//
//fun testLens() {
//    val employee = Employee(
//            "Иван",
//            Company(
//                    "Зелёные человечки",
//                    Address(
//                            "Тверь",
//                            Street(
//                                    5,
//                                    "Сергея Eсенина"
//                            )
//                    )
//            )
//    )
//
//    val streetNameLens: Lens<Employee, String> = Employee.company.address.street.name
//    Log.e("zzz", streetNameLens.set(employee, "Пушкина").toString())
//}
//package ru.lardis.meta
//
//import android.util.Log
//import ru.lardis.lens_core.Lens
//import ru.lardis.meta.model.*
//
//fun testLens() {
//    val employee = Employee(
//        "Иван",
//        Company(
//            "Зелёные человечки",
//            Address(
//                "Тверь",
//                Street(
//                    5,
//                    "Сергея Eсенина"
//                )
//            )
//        )
//    )
//
//    val streetNameLens: Lens<Employee, String> = Employee.company.address.street.name
//    Log.e("zzz", streetNameLens.set(employee, "Пушкина").toString())
//}
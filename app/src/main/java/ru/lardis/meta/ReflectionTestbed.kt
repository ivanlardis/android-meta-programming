 package ru.lardis.meta

 import android.util.Log
 import ru.lardis.lens_core.Lens
 import ru.lardis.lens_reflect.at
 import ru.lardis.lens_reflect.lens
 import ru.lardis.meta.model.Address
 import ru.lardis.meta.model.Company
 import ru.lardis.meta.model.Employee
 import ru.lardis.meta.model.Street

 fun testLens() {
     val employee = Employee(
         "Иван",
         Company(
             "Зелёные человечки",
             Address(
                 "Тверь",
                 Street(
                     5,
                     "Сергея Eсенина"
                 )
             )
         )
     )

     val streetNameLens: Lens<Employee, String> = lens(Employee::company)
         .at(Company::address)
         .at(Address::street)
         .at(Street::name)

     Log.e("zzz", streetNameLens.set(employee, "Пушкина").toString())
 }
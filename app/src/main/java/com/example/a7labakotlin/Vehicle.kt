package com.example.a7labakotlin

import android.os.Parcel
import android.os.Parcelable

// Данные класса Vehicle для хранения информации о транспортном средст
data class Vehicle(
    val brand: String,
    val model: String,
    val year: String,
    val type: String
) : Parcelable { // Реализация интерфейса Parcelable для передачи объектов Vehicle между компонентами
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    // Запись свойств объекта в Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(brand)
        parcel.writeString(model)
        parcel.writeString(year)
        parcel.writeString(type)
    }

    // Метод описания контента
    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Vehicle> {
        // Создание объекта Vehicle из Parcel
        override fun createFromParcel(parcel: Parcel): Vehicle {
            return Vehicle(parcel)
        }

        // Создание массива объектов Vehicle
        override fun newArray(size: Int): Array<Vehicle?> {
            return arrayOfNulls(size)
        }
    }
}
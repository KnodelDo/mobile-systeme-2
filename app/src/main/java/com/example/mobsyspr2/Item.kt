package com.example.mobsyspr2

import android.os.Parcel
import android.os.Parcelable

data class Item(
    val id: String = "",          // Standardwert für null
    val produkt: String = "Unbekannt", // Standardwert für null
    val menge: Int = 0,
    val notizen: String = "Keine Notizen" // Standardwert für null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",  // Standardwert für null
        parcel.readString() ?: "Unbekannt",  // Standardwert für null
        parcel.readInt(),
        parcel.readString() ?: "Keine Notizen"  // Standardwert für null
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(produkt)
        parcel.writeInt(menge)
        parcel.writeString(notizen)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Item> {
        override fun createFromParcel(parcel: Parcel): Item = Item(parcel)
        override fun newArray(size: Int): Array<Item?> = arrayOfNulls(size)
    }
}

package com.example.mobsyspr2

import android.os.Parcel
import android.os.Parcelable

data class Item(
    val id: String = "",
    val produkt: String = "Unbekannt",
    val menge: Int = 0,
    val notizen: String = "Keine Notizen"
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "Unbekannt",
        parcel.readInt(),
        parcel.readString() ?: "Keine Notizen"
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

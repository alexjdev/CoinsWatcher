package com.example.coinswatcher.utils

import android.os.Parcel
import android.os.Parcelable

data class CoinInfo(val id: String?, val name: String?, val symbol: String?, val rank: Int,
                    val price_usd: Double, val price_btc: Double, val h24_volume_usd: Double,
                    val market_cap_usd: Double, val available_supply: Double, val total_supply: Double,
                    val max_supply: Double, val percent_change_1h: Double, val percent_change_24h: Double,
                    val percent_change_7d: Double, val last_updated: Long) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(symbol)
        parcel.writeInt(rank)
        parcel.writeDouble(price_usd)
        parcel.writeDouble(price_btc)
        parcel.writeDouble(h24_volume_usd)
        parcel.writeDouble(market_cap_usd)
        parcel.writeDouble(available_supply)
        parcel.writeDouble(total_supply)
        parcel.writeDouble(max_supply)
        parcel.writeDouble(percent_change_1h)
        parcel.writeDouble(percent_change_24h)
        parcel.writeDouble(percent_change_7d)
        parcel.writeLong(last_updated)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CoinInfo> {
        override fun createFromParcel(parcel: Parcel): CoinInfo {
            return CoinInfo(parcel)
        }

        override fun newArray(size: Int): Array<CoinInfo?> {
            return arrayOfNulls(size)
        }
    }
}
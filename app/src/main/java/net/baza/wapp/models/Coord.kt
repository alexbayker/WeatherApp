package net.baza.wapp.models

import com.google.gson.annotations.SerializedName

class Coord(
    @SerializedName("lon")
    val lon: Double,

    @SerializedName("lat")
    val lat: Double
)
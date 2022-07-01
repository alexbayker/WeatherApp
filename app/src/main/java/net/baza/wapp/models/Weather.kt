package net.baza.wapp.models

import com.google.gson.annotations.SerializedName

class Weather (
    @SerializedName("id")
    val id: Int,

    /*@SerializedName("main")
    val main: String,*/

    @SerializedName("description")
    val desc: String,

    @SerializedName("icon")
    val icon: String
)
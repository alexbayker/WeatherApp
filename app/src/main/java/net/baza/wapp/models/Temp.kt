package net.baza.wapp.models

import com.google.gson.annotations.SerializedName
import net.baza.wapp.staticpackage.Constants

class Temp
{
    /*@SerializedName("min")
    var min: Double = 0.0
        get() = field - Constants.TEMP_DIFFERENT

    @SerializedName("max")
    var max: Double = 0.0
        get() = field - Constants.TEMP_DIFFERENT*/

    @SerializedName("morn")
    var morn: Double = 0.0
        get() = field - Constants.TEMP_DIFFERENT

    @SerializedName("day")
    var day: Double = 0.0
        get() = field - Constants.TEMP_DIFFERENT

    @SerializedName("eve")
    var eve: Double = 0.0
        get() = field - Constants.TEMP_DIFFERENT

    @SerializedName("night")
    var night: Double = 0.0
        get() = field - Constants.TEMP_DIFFERENT
}
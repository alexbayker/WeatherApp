package net.baza.wapp

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface API
{
    @GET
    fun apiRequest(@Url apiurl: String): Call<ResponseBody>

    @Streaming
    @GET
    fun downloadFile(@Url fileurl: String): Call<ResponseBody>

    @Multipart
    @POST
    fun uploadFile(@Url uploadurl: String, @Part file: MultipartBody.Part): Call<ResponseBody>
}
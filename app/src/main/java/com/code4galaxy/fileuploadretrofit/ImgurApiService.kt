package com.code4galaxy.fileuploadretrofit

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://api.imgur.com"

// Logging Interceptor
private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY  // Logs request & response body
}

// OkHttpClient with Interceptors
private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)  // Add logging
    .connectTimeout(15, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

// Retrofit Instance
private val retrofit = Retrofit.Builder()
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

// Imgur API Interface
interface ImgurApiService {
    @Multipart
    @POST("/3/upload")
    fun uploadImage(
        @Header("Authorization") clientId: String,
        @Part image: MultipartBody.Part
    ): Call<ImgurUploadJson>
}

// Singleton for API Access
object ImgurApi {
    val service: ImgurApiService by lazy {
        retrofit.create(ImgurApiService::class.java)
    }
}

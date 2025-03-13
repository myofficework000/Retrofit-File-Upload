package com.code4galaxy.fileuploadretrofit

import com.google.gson.annotations.SerializedName

data class ImgurUploadJson(
    @SerializedName("data") val data: Data?,
    @SerializedName("status") val status: Int?,
    @SerializedName("success") val success: Boolean?
) {
    fun getImageLink(): String? = data?.link
}

data class Data(
    val id: String,
    val link: String
)

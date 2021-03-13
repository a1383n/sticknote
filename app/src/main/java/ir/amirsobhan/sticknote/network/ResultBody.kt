package ir.amirsobhan.sticknote.network

import com.google.gson.annotations.SerializedName
import ir.amirsobhan.sticknote.database.User

data class ResultBody(
    @SerializedName("message") val message : String? = null,
    @SerializedName("user") val user: User? = null
)
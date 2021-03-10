package ir.amirsobhan.sticknote.network

import retrofit2.http.Field
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Field("email") email: String, @Field("password") password: String)
}
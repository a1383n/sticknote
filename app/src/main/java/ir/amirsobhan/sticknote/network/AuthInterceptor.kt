package ir.amirsobhan.sticknote.network

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import ir.amirsobhan.sticknote.database.User
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val user_json : String? = PreferenceManager.getDefaultSharedPreferences(context).getString("user",null)
        val user : User? = if (user_json != null) Gson().fromJson(user_json,User::class.java) else null
        val access_token : String? = if (user != null) user.access_token else null

        var req = chain.request()


        if (access_token.isNullOrEmpty()){
            val headers_list = Headers.of(mapOf(
                "Authorization" to "Bearer $access_token",
                "Accept" to "application/json"
            ))
            var headers = req.headers().newBuilder().addAll(headers_list).build()
            req = req.newBuilder().headers(headers).build()
            return chain.proceed(req)
        }else{
            return chain.proceed(req.newBuilder().header("Accept","application/json").build())
        }
    }
}
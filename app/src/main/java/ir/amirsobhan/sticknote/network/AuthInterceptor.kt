package ir.amirsobhan.sticknote.network

import android.content.Context
import androidx.preference.PreferenceManager
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val access_token = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("access_token",null)
        var req = chain.request()


        if (access_token != null){
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
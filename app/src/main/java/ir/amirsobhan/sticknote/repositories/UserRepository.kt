package ir.amirsobhan.sticknote.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import ir.amirsobhan.sticknote.AppExecutor
import ir.amirsobhan.sticknote.database.User
import ir.amirsobhan.sticknote.network.ResultBody
import ir.amirsobhan.sticknote.network.ApiService
import org.koin.java.KoinJavaComponent.inject
import retrofit2.Response
import java.util.concurrent.Callable

class UserRepository(val context: Context) {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val apiService : ApiService by inject(ApiService::class.java)

    val currentUser : User? get() = getCurrent()

    private fun getCurrent() : User? {
        val user = sharedPreferences.getString("user",null)
        return Gson().fromJson(user,User::class.java)
    }

    fun logoutUser(){
        sharedPreferences.edit().putString("user",null).apply()
    }

    fun loginUser(email : String,password : String): Response<ResultBody>? {
        val callable : Callable<Response<ResultBody>> = Callable { return@Callable apiService.login(email, password).execute() }
        return AppExecutor().networkIO().submit(callable).get()
    }

}
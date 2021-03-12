package ir.amirsobhan.sticknote.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import ir.amirsobhan.sticknote.database.User

class UserRepository(val context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val currentUser : User? get() = getCurrent()

    private fun getCurrent() : User? {
        val user = sharedPreferences.getString("user",null)
        return Gson().fromJson(user,User::class.java)
    }

    fun logout(){
        sharedPreferences.edit().putString("user",null).apply()
    }

    fun login(user: User){
        sharedPreferences.edit().putString("user",Gson().toJson(user))
    }

}
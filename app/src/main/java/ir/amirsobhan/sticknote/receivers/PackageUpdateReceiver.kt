package ir.amirsobhan.sticknote.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.edit
import ir.amirsobhan.sticknote.Constants
import org.koin.java.KoinJavaComponent.inject

class PackageUpdateReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        val sharedPreferences : SharedPreferences by inject(SharedPreferences::class.java)
        sharedPreferences.edit { putBoolean(Constants.SharedPreferences.IS_APP_UPDATE,true) }
    }
}
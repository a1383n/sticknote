package ir.amirsobhan.sticknote

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.database.AppDatabase
import ir.amirsobhan.sticknote.repositories.NoteRepository
import ir.amirsobhan.sticknote.viewmodel.CloudViewModel
import ir.amirsobhan.sticknote.viewmodel.NoteViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application(), androidx.work.Configuration.Provider{
    override fun onCreate() {
        super.onCreate()
        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(this)
        //Set App theme mode
        AppCompatDelegate.setDefaultNightMode(sharedPreference.getString("theme","-1")!!.toInt())

        val viewModelModules = module {
            viewModel { NoteViewModel(get()) }
            viewModel { CloudViewModel(this@App) }
        }


        val appModules = module {
            single { AppDatabase(this@App) }
            single { PreferenceManager.getDefaultSharedPreferences(this@App) }
            single { NoteRepository(get<AppDatabase>().noteDao()) }
            single { WorkManager.getInstance(this@App) }
        }


        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(viewModelModules,appModules)
        }

        //Setup firebase emulator
        //TODO: Remove for production
        if (BuildConfig.DEBUG && isEmulator()) {
            Firebase.auth.useEmulator("10.0.2.2", 9099)
            Firebase.firestore.useEmulator("10.0.2.2", 8080)
            Firebase.firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().apply { isPersistenceEnabled = false }.build()
        }
    }

    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator"))
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun getWorkManagerConfiguration(): androidx.work.Configuration {
        return androidx.work.Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build()
    }
}
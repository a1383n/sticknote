package ir.amirsobhan.sticknote

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import ir.amirsobhan.sticknote.database.AppDatabase
import ir.amirsobhan.sticknote.repositories.NoteRepository
import ir.amirsobhan.sticknote.viewmodel.NoteViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(this)

        //Set App theme mode
        AppCompatDelegate.setDefaultNightMode(sharedPreference.getString("theme","-1")!!.toInt())


        val appModules = module {

            single { AppDatabase(this@App) }
            single { AppExecutor() }

            single { NoteRepository(get<AppDatabase>().noteDao()) }

            viewModel { NoteViewModel(get()) }
        }

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModules)
        }
    }
}
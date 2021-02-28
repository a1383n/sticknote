package ir.amirsobhan.sticknote

import android.app.Application
import ir.amirsobhan.sticknote.viewmodel.NoteViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val appModules = module {
            viewModel { NoteViewModel(this@App) }
        }

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModules)
        }
    }
}
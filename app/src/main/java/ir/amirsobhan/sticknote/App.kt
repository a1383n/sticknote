package ir.amirsobhan.sticknote

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.preference.PreferenceManager
import ir.amirsobhan.sticknote.database.AppDatabase
import ir.amirsobhan.sticknote.network.ApiService
import ir.amirsobhan.sticknote.network.AuthInterceptor
import ir.amirsobhan.sticknote.repositories.NoteRepository
import ir.amirsobhan.sticknote.viewmodel.CloudViewModel
import ir.amirsobhan.sticknote.viewmodel.NoteViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class App : Application(){
    override fun onCreate() {
        super.onCreate()
        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(this)

        //Set App theme mode
        AppCompatDelegate.setDefaultNightMode(sharedPreference.getString("theme","-1")!!.toInt())



        val viewModelModules = module {
            viewModel { NoteViewModel(get()) }
            viewModel { CloudViewModel() }
        }


        val appModules = module {
            single { AppDatabase(this@App) }
            single { AppExecutor() }
            single { NoteRepository(get<AppDatabase>().noteDao()) }
        }

        val networkModules = module {
            factory { AuthInterceptor(this@App) }
            factory { provideOkHttpClient(get()) }
            factory { provideRetrofit(get()) }
            factory { provideApiService(get()) }
        }

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(viewModelModules,appModules,networkModules)
        }
    }

    private fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor(authInterceptor).build()
    }

    private fun provideRetrofit(okHttpClient: OkHttpClient) : Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.DEBUG_API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun provideApiService(retrofit: Retrofit) : ApiService = retrofit.create(ApiService::class.java)

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
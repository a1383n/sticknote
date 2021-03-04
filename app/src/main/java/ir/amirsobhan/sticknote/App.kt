package ir.amirsobhan.sticknote

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
            viewModel { CloudViewModel(this@App) }

            factory<GoogleSignInOptions> { GoogleSignInOptions.Builder().requestEmail().build() }
            factory<GoogleSignInClient> {GoogleSignIn.getClient(this@App,get())}


        }


        val networkModules = module {
            single { AuthInterceptor() }
            single { provideOkHttpClient(get()) }
            single { provideRetrofit(get()) }
            single { provideApiService(get()) }
        }

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModules,networkModules)
        }
    }

    // Network Providers
    fun provideOkHttpClient(authInterceptor: AuthInterceptor) : OkHttpClient{
        return OkHttpClient().newBuilder().addInterceptor(authInterceptor).build()
    }

    fun provideRetrofit(okHttpClient: OkHttpClient) : Retrofit{
        return Retrofit.Builder()
            .baseUrl("")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun provideApiService(retrofit: Retrofit) : ApiService{
        return retrofit.create(ApiService::class.java)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
package ir.amirsobhan.sticknote

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.perf.ktx.performance
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
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
        AppCompatDelegate.setDefaultNightMode(sharedPreference.getString(getString(R.string.setting_general_theme),"-1")!!.toInt())

        val viewModelModules = module {
            viewModel { NoteViewModel(get()) }
            viewModel { CloudViewModel() }
        }
        val appModules = module {
            single { AppDatabase(this@App) }
            single { PreferenceManager.getDefaultSharedPreferences(this@App) }
            single { NoteRepository(get<AppDatabase>().noteDao()) }
            single { WorkManager.getInstance(this@App) }
            single { GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(Constants.GOOGLE_ID_TOKEN)
                    .requestId()
                    .requestProfile()
                    .requestEmail()
                    .build()
            }

        }

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(viewModelModules,appModules)
        }


        initFirebase()

    }

    private fun initFirebase(){
        // Start and config firebase services
        FirebaseApp.initializeApp(this)
        val appCheck = FirebaseAppCheck.getInstance()
        appCheck.installAppCheckProviderFactory(SafetyNetAppCheckProviderFactory.getInstance())

        Firebase.remoteConfig.setDefaultsAsync(mapOf(
            Constants.RemoteConfig.APP_VERSION to BuildConfig.VERSION_CODE,
            Constants.RemoteConfig.FETCH_INTERVAL to if (BuildConfig.DEBUG) 0 else 3600
        ))

        Firebase.auth.currentUser?.reload()
        Firebase.analytics
        Firebase.performance
        Firebase.messaging

        Firebase.auth.currentUser?.uid?.let {
            Firebase.crashlytics.setUserId(it)
            Firebase.analytics.setUserId(it)
            Firebase.auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        }

        Firebase.remoteConfig.setConfigSettingsAsync(remoteConfigSettings { minimumFetchIntervalInSeconds = Firebase.remoteConfig[Constants.RemoteConfig.FETCH_INTERVAL].asLong() })
        Firebase.remoteConfig.fetchAndActivate()

        if(Firebase.auth.currentUser != null){
            Firebase.messaging.token.addOnSuccessListener {
                Firebase.firestore.document(Constants.CloudDatabase.getDocumentPath(Firebase.auth.uid))
                    .set(hashMapOf(
                        Constants.CloudDatabase.MESSAGE_TOKEN to listOf(it)
                    ), SetOptions.merge())
            }
        }

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN,null)
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
package ir.amirsobhan.sticknote

import android.content.Context
import android.content.res.Configuration
import org.koin.java.KoinJavaComponent.inject

object Constants {
    const val APP_DATABASE_NAME = "app-database"

    object SharedPreferences{
        const val LAST_SYNC = "last_sync"
        const val IS_APP_UPDATE = "is_app_update"
        const val ENCRYPTION_READY = "encryption_redy"
    }

    object CloudDatabase{
        fun getDocumentPath(uid : String?) = "users/$uid"
        const val NOTE_FIELD = "notes"
        const val LAST_SYNC_FIELD = "last_sync"
        const val MESSAGE_TOKEN = "message_token"
    }

    object RemoteConfig{
        const val APP_VERSION = "app_version"
        const val FETCH_INTERVAL = "fetch_interval"
    }

    object Encryption{
        private val sharedPreferences by inject<android.content.SharedPreferences>(android.content.SharedPreferences::class.java)
        const val ALGORITHM = "AES"
        const val TRANSFORMATION = "AES/ECB/PKCS5Padding"
        fun isReady() = sharedPreferences.getBoolean(SharedPreferences.ENCRYPTION_READY,false)
    }

    const val GOOGLE_ID_TOKEN = "407197468075-hvl6n5tldj9pngajraeevhkegt4ndu9q.apps.googleusercontent.com"

    fun isDarkMode(context: Context) : Boolean{
        return context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    const val NOTE_ACTIVITY_EXTRA_INPUT = "note_json"
    const val DOWNLOAD_CONTROLLER_APP_NAME = "stickMeNote.apk"
}
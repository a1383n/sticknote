package ir.amirsobhan.sticknote.viewmodel

import android.content.SharedPreferences
import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.Constants
import ir.amirsobhan.sticknote.worker.AutoSync
import org.koin.java.KoinJavaComponent.inject

class CloudViewModel : ViewModel() {

    companion object{
        const val LOGIN_REQ_CODE = 10
    }

    private val sharedPreferences : SharedPreferences by inject(SharedPreferences::class.java)
    private val lastSync : Long get() = sharedPreferences.getLong(Constants.SharedPreferences.LAST_SYNC,0L)
    private val workManager : WorkManager by inject(WorkManager::class.java)
    private val work = AutoSync.Factory(AutoSync.SYNC)

    fun getLastSyncDate() : CharSequence? {
        return if (sharedPreferences.getLong(Constants.SharedPreferences.LAST_SYNC,0L) != 0L){
            DateUtils.getRelativeTimeSpanString(lastSync,System.currentTimeMillis(),0)
        }else{
            "Never"
        }
    }

    fun startSyncWork(): LiveData<WorkInfo> {
        workManager.enqueue(work)
        return workManager.getWorkInfoByIdLiveData(work.id)
    }

    fun handleActivityResult(){
        workManager.enqueue(AutoSync.Factory(AutoSync.SYNC))
        Firebase.auth.uid?.let {
            Firebase.crashlytics.setUserId(it)
            Firebase.analytics.setUserId(it)
        }
    }
}
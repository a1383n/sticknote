package ir.amirsobhan.sticknote.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateUtils
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.repositories.NoteRepository
import org.koin.java.KoinJavaComponent.inject

class CloudViewModel(val context : Context) : ViewModel() {
    private val firestore : FirebaseFirestore = Firebase.firestore
    private val repository : NoteRepository by inject(NoteRepository::class.java)
    private val sharedPreferences : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getLastSyncDate() : String{
        if (sharedPreferences.getLong("last_sync",0L) != 0L){
            return "Last sync:" + DateUtils.getRelativeTimeSpanString(
                    sharedPreferences.getLong("last_sync",0L),System.currentTimeMillis(),0
            ).toString()
        }else{
            return "Last sync: Never"
        }
    }

    fun putNotesToRemote(): Task<Void> {
        val list : List<Note> = repository.exportAll()

        return firestore.collection("users").document(Firebase.auth.currentUser.uid)
                .set(hashMapOf(
                        "notes" to list,
                        "last_sync" to System.currentTimeMillis()
                ))
                .addOnSuccessListener { sharedPreferences.edit { putLong("last_sync",System.currentTimeMillis()).apply() } }
    }

}
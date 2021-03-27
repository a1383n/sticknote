package ir.amirsobhan.sticknote.worker

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.repositories.NoteRepository
import org.koin.java.KoinJavaComponent

class AutoSync(val context: Context,workerParameters: WorkerParameters) : Worker(context,workerParameters){
    override fun doWork(): Result {
        //Initialization FireStore database & note repository
        val firestore : FirebaseFirestore = Firebase.firestore
        val repository : NoteRepository by KoinJavaComponent.inject(NoteRepository::class.java)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        //Import all notes saved in local to db
        firestore.collection("users").document(Firebase.auth.currentUser.uid)
                .set(hashMapOf("notes" to repository.exportAll() , "last_sync" to System.currentTimeMillis()))
                .addOnSuccessListener { sharedPreferences.edit { putLong("last_sync",System.currentTimeMillis()).apply() } }

        return Result.success()
    }
}
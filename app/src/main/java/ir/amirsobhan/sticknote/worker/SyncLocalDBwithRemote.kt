package ir.amirsobhan.sticknote.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SyncLocalDBwithRemote(context: Context, workerParams: WorkerParameters) : Worker(context,workerParams) {
    override fun doWork(): Result {
        val remoteDB = Firebase.firestore

        remoteDB.collection("users").document(Firebase.auth.currentUser.uid)
            .get(Source.SERVER).addOnSuccessListener {
                if (inputData.getString("json")?.length != it["notes"].toString().length){
                    remoteDB.collection("users").document(Firebase.auth.currentUser.uid)
                        .set(hashMapOf("notes" to inputData.getString("json"), "timestamp" to System.currentTimeMillis()))
                }
            }

        return Result.success()
    }
}
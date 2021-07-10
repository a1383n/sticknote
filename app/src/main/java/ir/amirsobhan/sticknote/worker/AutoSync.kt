package ir.amirsobhan.sticknote.worker

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.util.Log
import androidx.work.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.repositories.NoteRepository
import org.koin.java.KoinJavaComponent.inject
import java.util.*

class AutoSync(val context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    companion object {
        const val SET: Int = 1
        const val GET: Int = 2
        const val DELETE: Int = 3
        const val SYNC: Int = 4
        const val TAG = "sync"

        fun Factory(action : Int,id : String = "") : OneTimeWorkRequest{
            return OneTimeWorkRequestBuilder<AutoSync>()
                    .addTag("sync")
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .setInputData(Data.Builder().putAll(mapOf("action" to action, "id" to id)).build())
                    .build()
        }
    }

    //Initialization FireStore database & note repository
    val firestore: FirebaseFirestore = Firebase.firestore
    val repository: NoteRepository by inject(NoteRepository::class.java)
    val sharedPreferences: SharedPreferences by inject(SharedPreferences::class.java)
    val auth = Firebase.auth
    var TAG = "AutoSync"
    var action: Int = inputData.getInt("action", SYNC)
    lateinit var doc : DocumentReference


    override fun doWork(): Result {
        if (auth.currentUser == null) {
            Log.d(TAG,"User not login")
            return Result.failure()
        }else{
            doc = firestore.document("users/${Firebase.auth.currentUser?.uid}")
        }

        when (action) {
            GET -> getNotesFromRemote()
            SET -> setNotesToRemote(repository.exportAll())
            SYNC -> syncNotesToRemote()
            DELETE -> deleteNoteFromEveryWhere(inputData.getString("id")!!)
        }

        return Result.success()
    }

    fun getNotesFromRemote() {
        val remoteList = mutableListOf<Note>()
        val dbList = repository.exportAll().toMutableList()
        doc.get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        (it["notes"] as List<HashMap<String, Objects>>).forEach { remoteList.add(Note.fromHashMap(it)) }
                    }

                    if (remoteList.deepEquals(dbList) && action != SYNC) {
                        Log.d(TAG + "/GET", "Work canceled")
                    } else {
                        repository.insertAll(remoteList)
                        Log.d(TAG + "/GET", remoteList.size.toString())
                    }

                }

    }

    fun setNotesToRemote(list: List<Note>) {
        val time = System.currentTimeMillis()
        Log.d(TAG + "/SET", list.size.toString())
        doc.set(hashMapOf(
                "last_sync" to Timestamp(Date(time)),
                "notes" to list
        ), SetOptions.merge()).addOnSuccessListener {
            sharedPreferences.edit().putLong("last_sync", time).apply()
        }
    }

    fun syncNotesToRemote() {
        val remoteList = mutableListOf<Note>()
        val dbList = repository.exportAll().toMutableList()
        doc.get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        (it["notes"] as List<HashMap<String, Objects>>).forEach { remoteList.add(Note.fromHashMap(it)) }
                    }

                    Log.d(TAG + "/SYNC", "Remote To Local :"+remoteList.size)
                    repository.insertAll(remoteList)

                    Handler().postDelayed(Runnable { setNotesToRemote(repository.exportAll()) },1000)

                }
    }

    fun deleteNoteFromEveryWhere(id : String) {
        val remoteList = mutableListOf<Note>()

        doc.get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        (it["notes"] as List<HashMap<String, Objects>>).forEach { remoteList.add(Note.fromHashMap(it)) }
                        if (id == null) {
                            Log.d(TAG + "/DELETE",inputData.toString())
                        }else{
                            for(note : Note in remoteList){
                                if (note.id == id){
                                    remoteList.remove(note)
                                    break
                                }
                            }

                            Log.d(TAG + "/DELETE",id)
                            setNotesToRemote(remoteList)
                        }

                    }
                }
    }

    fun List<*>.deepEquals(other: List<*>): Boolean {
        return this.size == other.size && this.mapIndexed { index, any -> any == other[index] }.all { it }
    }
}
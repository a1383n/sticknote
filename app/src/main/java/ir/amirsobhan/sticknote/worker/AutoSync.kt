package ir.amirsobhan.sticknote.worker

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.work.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.Constants
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.repositories.NoteRepository
import org.koin.java.KoinJavaComponent.inject
import java.util.*

class AutoSync(val context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    companion object {
        const val SET = 1
        const val GET = 2
        const val DELETE = 3
        const val SYNC = 4
        const val TAG = "sync"
        private const val ACTION = "action"
        private const val NOTE_ID = "id"

        fun Factory(action : Int,id : String = "") : OneTimeWorkRequest{
            return OneTimeWorkRequestBuilder<AutoSync>()
                    .addTag(TAG)
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .setInputData(Data.Builder().putAll(mapOf(ACTION to action, NOTE_ID to id)).build())
                    .build()
        }
    }

    //Initialization Firestore database & note repository
    val firestore: FirebaseFirestore = Firebase.firestore
    private val repository: NoteRepository by inject(NoteRepository::class.java)
    private val sharedPreferences: SharedPreferences by inject(SharedPreferences::class.java)
    val auth = Firebase.auth
    var action: Int = inputData.getInt(ACTION, SYNC)
    lateinit var doc : DocumentReference


    override fun doWork(): Result {
        if (auth.currentUser == null) {
            return Result.failure()
        }else{
            doc = firestore.document(Constants.CloudDatabase.getDocumentPath(auth.uid))
        }

        when (action) {
            GET -> getNotesFromRemote()
            SET -> setNotesToRemote(repository.exportAll())
            SYNC -> syncNotesToRemote()
            DELETE -> deleteNoteFromEveryWhere(inputData.getString(NOTE_ID)!!)
        }

        return Result.success()
    }

    private fun getNotesFromRemote() {
        val remoteList = mutableListOf<Note>()
        val dbList = repository.exportAll().toMutableList()
        doc.get()
                .addOnSuccessListener { it ->
                    if (it.exists()) {
                            try {
                                (it["notes"] as List<HashMap<String, Objects>>).forEach { remoteList.add(Note.fromHashMap(it)) }
                            }catch (e : Exception){}
                    }

                    if (!remoteList.deepEquals(dbList) || action == SYNC) {
                        repository.deleteAll()
                        repository.insertAll(remoteList)
                        Log.d(TAG + "/GET", remoteList.size.toString())
                    }
                }

    }

    private fun setNotesToRemote(list: List<Note>) {
        val time = System.currentTimeMillis()
        doc.set(hashMapOf(
                Constants.CloudDatabase.LAST_SYNC_FIELD to Timestamp(Date(time)),
                Constants.CloudDatabase.NOTE_FIELD to list
        ), SetOptions.merge()).addOnSuccessListener {
            sharedPreferences.edit().putLong(Constants.SharedPreferences.LAST_SYNC, time).apply()
        }
    }

    private fun syncNotesToRemote() {
        val remoteList = mutableListOf<Note>()
        doc.get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        try {
                            (it[Constants.CloudDatabase.NOTE_FIELD] as List<HashMap<String, Objects>>).forEach { remoteList.add(Note.fromHashMap(it)) }
                        }catch (e : Exception){}
                    }

                    repository.insertAll(remoteList)

                    Handler(Looper.getMainLooper()).postDelayed({ setNotesToRemote(repository.exportAll()) },1000)
                }
    }

    private fun deleteNoteFromEveryWhere(id : String) {
        val remoteList = mutableListOf<Note>()

        doc.get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        (it["notes"] as List<HashMap<String, Objects>>).forEach { remoteList.add(Note.fromHashMap(it)) }

                        remoteList.forEach {
                            if (it.id == id){
                                remoteList.remove(it)
                                return@forEach
                            }
                        }

                        setNotesToRemote(remoteList)
                    }
                }
    }

    private fun List<*>.deepEquals(other: List<*>): Boolean {
        return this.size == other.size && this.mapIndexed { index, any -> any == other[index] }.all { it }
    }
}
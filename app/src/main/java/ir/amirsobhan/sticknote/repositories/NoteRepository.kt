
package ir.amirsobhan.sticknote.repositories

import androidx.lifecycle.LiveData
import androidx.work.WorkManager
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.database.NoteDao
import ir.amirsobhan.sticknote.diskIO
import ir.amirsobhan.sticknote.worker.AutoSync
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.Callable

class NoteRepository(private val noteDao: NoteDao) {
    val workManager : WorkManager by inject(WorkManager::class.java)
    fun exportAll() : List<Note>{
        val callable : Callable<List<Note>> = Callable { return@Callable noteDao.exportAll() } 
        return diskIO().submit(callable).get()
    }

    fun getAll() : LiveData<List<Note>> {
        var callable: Callable<LiveData<List<Note>>> = Callable { return@Callable noteDao.getAll() }
        return diskIO().submit(callable).get()
    }

    fun getByID(id : String) : Note?{
        var callable : Callable<Note> = Callable { return@Callable noteDao.getByID(id) }
        return diskIO().submit(callable).get()
    }

    fun insertAll(noteList: List<Note>){
        diskIO().submit(Runnable { noteDao.insertAll(noteList) })
    }

    fun insert(note: Note){
        diskIO().submit(Runnable { noteDao.insert(note) })
        workManager.enqueue(AutoSync.Factory(AutoSync.SET))
    }

    fun update(note: Note){
        diskIO().submit(Runnable { noteDao.update(note) })
        workManager.enqueue(AutoSync.Factory(AutoSync.SET))
    }

    fun delete(note: Note){
        diskIO().submit(Runnable { noteDao.delete(note) })
        workManager.enqueue(AutoSync.Factory(AutoSync.DELETE,note.id))
    }
}
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
    private val workManager : WorkManager by inject(WorkManager::class.java)

    fun exportAll() : List<Note>{
        val callable : Callable<List<Note>> = Callable { return@Callable noteDao.exportAll() } 
        return diskIO().submit(callable).get()
    }

    fun getAll() : LiveData<List<Note>> {
        val callable: Callable<LiveData<List<Note>>> = Callable { return@Callable noteDao.getAll() }
        return diskIO().submit(callable).get()
    }

    fun getNoteByID(id : String) : Note{
        val callable : Callable<Note> = Callable { return@Callable noteDao.getByID(id) }
        return diskIO().submit(callable).get()
    }

    fun insertAll(noteList: List<Note>){
        diskIO().submit { noteDao.insertAll(noteList) }
        workManager.enqueue(AutoSync.Factory(AutoSync.SET))
    }

    fun deleteAll(){
        diskIO().submit { noteDao.deleteAll() }
    }

    fun insert(note: Note){
        diskIO().submit { noteDao.insert(note) }
        workManager.enqueue(AutoSync.Factory(AutoSync.SET))
    }

    fun update(note: Note){
        diskIO().submit { noteDao.update(note) }
        workManager.enqueue(AutoSync.Factory(AutoSync.SET))
    }

    fun delete(note: Note){
        diskIO().submit { noteDao.delete(note) }
        workManager.enqueue(AutoSync.Factory(AutoSync.DELETE, arrayOf(note.id)))
    }

    fun deleteByID(array: Array<String>){
        diskIO().submit { noteDao.deleteNotesByID(array) }
        workManager.enqueue(AutoSync.Factory(AutoSync.DELETE,array))
    }
}
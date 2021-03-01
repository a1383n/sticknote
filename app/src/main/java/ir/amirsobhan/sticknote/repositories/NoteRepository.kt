package ir.amirsobhan.sticknote.repositories

import androidx.lifecycle.LiveData
import ir.amirsobhan.sticknote.AppExecutor
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.database.NoteDao
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.Callable

class NoteRepository(private val noteDao: NoteDao) {

    val appExecutor : AppExecutor by inject(AppExecutor::class.java)

    fun getAll() : LiveData<List<Note>> {
        var callable: Callable<LiveData<List<Note>>> = Callable { return@Callable noteDao.getAll() }
        return appExecutor.diskIO().submit(callable).get()
    }

    fun insert(note: Note){
        appExecutor.diskIO().submit(Runnable { noteDao.insert(note) })
    }

    fun update(note: Note){
        appExecutor.diskIO().submit(Runnable { noteDao.update(note) })
    }

    fun delete(note: Note){
        appExecutor.diskIO().submit(Runnable { noteDao.delete(note) })
    }
}
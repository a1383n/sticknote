package ir.amirsobhan.sticknote.repositories

import android.app.Application
import ir.amirsobhan.sticknote.AppExecutor
import ir.amirsobhan.sticknote.database.AppDatabase
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.database.NoteDao
import java.util.concurrent.Callable

class NoteRepository(private val noteDao: NoteDao) {
    companion object {
        @Volatile
        private var instances: NoteRepository? = null
        private val LOCK = Any()

        operator fun invoke(application: Application) = instances ?: synchronized(LOCK) {
            instances ?: build(application).also { instances = it }
        }

        private fun build(application: Application) = NoteRepository(AppDatabase.invoke(application).noteDao())
    }

    fun getAll() : List<Note> {
        var callable: Callable<List<Note>> = Callable { return@Callable noteDao.getAll() }
        return AppExecutor.invoke().diskIO().submit(callable).get()
    }
}
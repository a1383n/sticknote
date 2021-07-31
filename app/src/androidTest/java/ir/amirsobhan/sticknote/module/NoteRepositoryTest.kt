package ir.amirsobhan.sticknote.module

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import ir.amirsobhan.sticknote.database.AppDatabase
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.repositories.NoteRepository
import org.junit.After
import org.junit.Before
import org.junit.Test

class NoteRepositoryTest{
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var repository: NoteRepository
    private lateinit var database : AppDatabase
    private val note : Note get() = Note(text = "Test body",title = "Test title")


    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        repository = NoteRepository(database.noteDao())
    }


    @Test
    fun insertToDatabaseTest(){
        repository.insert(note)
    }

    @Test
    fun readFromDatabase(){
        val note = this.note
        repository.insert(note)
        assertThat(repository.getNoteByID(note.id)).isEqualTo(note)
    }

    @Test
    fun deleteFromDatabase(){
        repository.delete(note)
        assertThat(repository.exportAll().size).isEqualTo(0)
    }

    @Test
    fun insertMultipleToDatabase(){
        repository.insertAll(listOf(note,note,note))
        assertThat(repository.exportAll().size).isAtMost(3)
    }

    @Test
    fun deleteAllFromDatabase() {
        repository.deleteAll()
        assertThat(repository.exportAll().size).isEqualTo(0)
    }

    @After
    fun closeDB(){
        database.close()
    }
}
package ir.amirsobhan.sticknote

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import ir.amirsobhan.sticknote.database.AppDatabase
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.database.NoteDao
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var database : AppDatabase
    private lateinit var dao : NoteDao
    private val note : Note get() = Note(text = "Test body",title = "Test title")
    
    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        dao = database.noteDao()
    }
    
    @Test
    fun insertToDatabaseTest(){
        dao.insert(note)
    }

    @Test
    fun readFromDatabase(){
        val note = this.note
        dao.insert(note)
        assertThat(dao.getByID(note.id)).isEqualTo(note)
    }

    @Test
    fun deleteFromDatabase(){
        dao.delete(note)
        assertThat(dao.exportAll().size).isEqualTo(0)
    }

    @Test
    fun insertMultipleToDatabase(){
        dao.insertAll(listOf(note,note,note))
        assertThat(dao.exportAll().size).isEqualTo(3)
    }

    @Test
    fun deleteAllFromDatabase() {
        dao.deleteAll()
        assertThat(dao.exportAll().size).isEqualTo(0)
    }

    @Test
    fun updateNoteFromDatabase(){
        val note = this.note
        dao.insert(note)
        note.apply {
            title = "Updated"
            text = "Updated body"
        }
        dao.update(note)

        assertThat(dao.getByID(note.id)).isEqualTo(note)
    }

    @After
    fun closeDB(){
        database.close()
    }
}
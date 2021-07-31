package ir.amirsobhan.sticknote.module

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import ir.amirsobhan.sticknote.database.AppDatabase
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.repositories.NoteRepository
import ir.amirsobhan.sticknote.utils.getOrAwaitValue
import ir.amirsobhan.sticknote.viewmodel.NoteViewModel
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteViewModelTest{
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var database : AppDatabase
    private val note = Note(title = "Title",text = "Body")

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        noteViewModel = NoteViewModel(NoteRepository(database.noteDao()))
    }

    @Test
    fun testViewModel(){
        database.noteDao().insert(note)
        val result = noteViewModel.notes.getOrAwaitValue().find {
            note == it
        }

        assertThat(result != null).isTrue()
    }

    @After
    fun closeDB(){
        database.close()
    }
}
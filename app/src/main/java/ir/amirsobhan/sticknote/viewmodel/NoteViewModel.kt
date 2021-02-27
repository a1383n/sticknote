package ir.amirsobhan.sticknote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.repositories.NoteRepository

class NoteViewModel(private val app: Application) : AndroidViewModel(app) {
    val notes : LiveData<List<Note>> by lazy { getAllNote() }

    private fun getAllNote() : LiveData<List<Note>> = NoteRepository.invoke(app).getAll()

    fun insert(note: Note){
        NoteRepository.invoke(app).insert(note)
    }
}
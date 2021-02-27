package ir.amirsobhan.sticknote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.repositories.NoteRepository

class NoteViewModel(private val app: Application) : AndroidViewModel(app) {
    private val repository : NoteRepository = NoteRepository.invoke(app)
    val notes : LiveData<List<Note>> by lazy { getAllNote() }

    private fun getAllNote() : LiveData<List<Note>> = repository.getAll()

    fun insert(note: Note){
        repository.insert(note)
    }

    fun update(note: Note){
        repository.update(note)
    }


    fun delete(note: Note){
        repository.delete(note)
    }
}
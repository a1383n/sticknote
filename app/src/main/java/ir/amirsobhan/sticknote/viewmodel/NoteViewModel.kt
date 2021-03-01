package ir.amirsobhan.sticknote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.repositories.NoteRepository

class NoteViewModel(val repository: NoteRepository) : ViewModel() {
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
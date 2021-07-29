package ir.amirsobhan.sticknote.viewmodel

import android.content.Context
import android.content.Intent
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.adapters.NoteAdapter
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.repositories.NoteRepository

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    val notes: LiveData<List<Note>> by lazy { getAllNote() }
    var adapter: NoteAdapter? = null
    var actionMode: ActionMode? = null
    val selectedNote: MutableLiveData<MutableList<Note>> = MutableLiveData(mutableListOf())
    private val selectedNoteValue get() = selectedNote.value!!

    private fun getAllNote(): LiveData<List<Note>> = repository.getAll()

    fun insert(note: Note) {
        repository.insert(note)
    }

    fun update(note: Note) {
        repository.update(note)
    }

    fun delete(note: Note) {
        repository.delete(note)
    }

    fun getNoteByID(id: String): Note {
        return repository.getNoteByID(id)
    }

    fun actionModeCallback(context: Context): ActionMode.Callback {
        return object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                MenuInflater(context).inflate(R.menu.note_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                when (item?.itemId) {
                    R.id.note_delete -> deleteMultiNote(context)
                    R.id.note_share -> shareMultiNote(context, selectedNoteValue)
                }

                return true
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                selectedNote.postValue(mutableListOf())
            }

        }
    }

    fun deleteMultiNote(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setMessage(
                context.getString(
                    R.string.deleteMultiNote_message,
                    selectedNote.value!!.size.toString()
                )
            )
            .setPositiveButton(R.string.yes) { _, _ ->
                selectedNote.value!!.forEach { delete(it) }
                actionMode?.finish()
            }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
    }

    fun shareMultiNote(context: Context, noteList: List<Note>) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_HTML_TEXT, noteList.map {
                if (it.title != null) {
                    return@map "${it.title} \n ${it.text} \n\n"
                } else {
                    return@map it.text.toString()
                }
            }.joinToString { it })
            putExtra(Intent.EXTRA_TEXT, noteList.map {
                if (it.title != null) {
                    return@map "${it.title} \n ${it.text?.replace(Regex("\\<.*?\\>"),"")} \n\n"
                } else {
                    return@map it.text?.replace(Regex("\\<.*?\\>"),"").toString()
                }
            }.joinToString { it })
            type = "text/html"
        }

        context.startActivity(
            Intent.createChooser(
                shareIntent,
                context.getString(R.string.send_notes_to)
            )
        )
    }
}
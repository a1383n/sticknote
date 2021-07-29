package ir.amirsobhan.sticknote.ui.activity

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import ir.amirsobhan.sticknote.Constants
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.databinding.ActivityNoteBinding
import ir.amirsobhan.sticknote.helper.KeyboardManager
import ir.amirsobhan.sticknote.viewmodel.NoteViewModel
import org.koin.android.ext.android.inject

class NoteActivity : AppCompatActivity() ,View.OnClickListener {
    private lateinit var binding: ActivityNoteBinding
    private val noteViewModel : NoteViewModel by inject()
    private lateinit var keyboardManager: KeyboardManager
    private lateinit var note: Note
    private var fromIntent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        keyboardManager = KeyboardManager(this)

        handleIntent()
        setupAppBar()
        setEditText()
    }

    private fun handleIntent() {
        // Check activity start from app or not
        if (intent.action == Intent.ACTION_SEND){
            // Activity start from share option and had extra text
            note = Note(text = intent.extras?.get(Intent.EXTRA_TEXT).toString(),title = "")
        }else {
            val id = intent.getStringExtra(Constants.NOTE_ACTIVITY_EXTRA_INPUT)
            // Activity start from app and had extra input
            note = if (id != null) {
                noteViewModel.getNoteByID(id).also { fromIntent = true }
            } else {
                // Empty note
                Note(title = "", text = "")
            }
        }
    }

    private fun setupAppBar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        if (binding.toolbarEditText.hint == getString(R.string.note_activity_untitled) && binding.toolbarEditText.text.isEmpty()
        ) binding.collapsingToolbarLayout.title = getString(R.string.note_activity_untitled)
        else binding.collapsingToolbarLayout.title = binding.toolbarEditText.text.toString()


        if (fromIntent){
            binding.toolbar.inflateMenu(R.menu.edit_note)
        }else{
            binding.toolbar.inflateMenu(R.menu.add_note)
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.save -> onBackPressed()
                R.id.delete -> deleteNote()
                R.id.share -> updateNote().also { noteViewModel.shareMultiNote(this, listOf(note)) }
            }

            true
        }

        binding.body.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY > 200){
                binding.appBarLayout.setExpanded(false,true)
            }else{
                binding.appBarLayout.setExpanded(true,true)
            }
        }
    }

    private fun setEditText() {
        binding.textStyleBar.setEditor(binding.body)
        binding.body.isVerticalScrollBarEnabled = true
        binding.body.html = note.text

        binding.body.setEditorBackgroundColor(Color.TRANSPARENT)
        if (Constants.isDarkMode(this)){
            binding.body.setEditorFontColor(Color.WHITE)
        }
    }

    private fun insert() {
        noteViewModel.insert(Note(title = binding.toolbarEditText.text.toString(), text = binding.body.html))
        Toast.makeText(this, R.string.note_activity_note_saved, Toast.LENGTH_LONG).show()
    }

    private fun updateNote() {
        noteViewModel.update(note.apply {
            text = binding.body.html
            title = binding.toolbarEditText.text.toString()
            timestamp = Timestamp.now()
        })
    }

    private fun deleteNote(){
        MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
            .setTitle(R.string.note_activity_delete_note_message)
            .setPositiveButton(R.string.yes) { _, _ -> noteViewModel.delete(note).also { finish() }}
            .setNegativeButton(R.string.no,null)
            .show()
    }

    override fun onBackPressed() {
        keyboardManager.closeKeyboard(binding.root)
        if (fromIntent) {
            //Is note changed ?
            if (binding.toolbarEditText.text.toString() != note.title || binding.body.html != note.text) {
                // Yes , Show dialog to the user to confirm this change
                    MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
                        .setMessage(R.string.note_activity_note_overwrite)
                        .setPositiveButton(R.string.save) { _: DialogInterface, _: Int -> updateNote().also { super.onBackPressed() }}
                        .setNegativeButton(R.string.discard) { _: DialogInterface, _: Int -> finish() }
                        .setNeutralButton(R.string.cancel, null)
                        .show()
            }else{
                super.onBackPressed()
            }
        } else if (!binding.body.html.isNullOrEmpty() || binding.toolbarEditText.text.isNotEmpty()) {
            insert()
            super.onBackPressed()
        }else{
            super.onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        Toast.makeText(this, v?.id.toString(), Toast.LENGTH_SHORT).show()
    }

}

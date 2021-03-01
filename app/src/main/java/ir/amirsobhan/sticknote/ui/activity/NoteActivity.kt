package ir.amirsobhan.sticknote.ui.activity

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.databinding.ActivityNoteBinding
import ir.amirsobhan.sticknote.helper.KeyboardManager
import ir.amirsobhan.sticknote.viewmodel.NoteViewModel

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    val noteViewModel : NoteViewModel by viewModels()
    private lateinit var keyboardManager: KeyboardManager
    private lateinit var note: Note
    var fromIntent = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        keyboardManager = KeyboardManager(this)

        loadFromIntent()
        setupAppBar()
        setEditText()


    }

    private fun loadFromIntent() {
        val json = intent.getStringExtra("json")
        if (json != null) {
            fromIntent = true
            note = Gson().fromJson(json, Note::class.java)

            binding.toolbarEditText.setText(note.title)
            binding.body.setText(note.text)
            binding.collapsingToolbarLayout.title = note.title
        }else{
            note = Note(null,"","",System.currentTimeMillis())
        }
    }

    private fun setupAppBar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        if (binding.toolbarEditText.hint == getString(R.string.untitled) && TextUtils.isEmpty(
                binding.toolbarEditText.text.toString()
            )
        ) binding.collapsingToolbarLayout.title =
            getString(R.string.untitled)
        else binding.collapsingToolbarLayout.title = binding.toolbarEditText.text.toString()


        if (fromIntent){
            binding.toolbar.inflateMenu(R.menu.edit_note)
        }else{
            binding.toolbar.inflateMenu(R.menu.add_note)
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.save -> onBackPressed()
                R.id.delete -> deleteDB()
            }

            true
        }
    }

    private fun setEditText() {
        binding.toolbarEditText.requestFocus()
        keyboardManager.showKeyboard(binding.toolbarEditText)

        binding.body.setOnClickListener {
            if (binding.toolbarEditText.isFocused) {
                binding.toolbarEditText.clearFocus()
                keyboardManager.closeKeyboard(binding.toolbarEditText)
                binding.collapsingToolbarLayout.title = binding.toolbarEditText.text.toString()
            }
        }
    }

    private fun insertToDB() {
        noteViewModel.insert(
            Note(
                null,
                binding.toolbarEditText.text.toString(),
                binding.body.text.toString(),
                System.currentTimeMillis()
            )
        )

        Toast.makeText(this, "Your note saved successfully", Toast.LENGTH_LONG).show()

    }

    private fun updateDB() {
        noteViewModel.update(note!!.apply {
            text = binding.body.text.toString()
            title = binding.toolbarEditText.text.toString()
            timestamp = System.currentTimeMillis()
        })
    }

    private fun deleteDB(){
        MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
            .setTitle("Are you sure you want to delete this note ?")
            .setPositiveButton("Yes") {dialog,witch -> noteViewModel.delete(note).also { finish() }}
            .setNegativeButton("No",null)
            .show()
    }

    override fun onBackPressed() {
        keyboardManager.closeKeyboard(binding.body)
        keyboardManager.closeKeyboard(binding.toolbarEditText)
        if (fromIntent) {
            //Is note changed ?
            if (binding.toolbarEditText.text.toString() != note?.title || binding.body.text.toString() != note?.text) {
                // Yes , Show dialog to the user to confirm this change
                    MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
                        .setTitle("Save this note ?")
                        .setMessage("Are you sure you want to save the new note?")
                        .setPositiveButton("Save") { dialogInterface: DialogInterface, i: Int -> updateDB().also { super.onBackPressed() }}
                        .setNegativeButton("Discard") { dialogInterface: DialogInterface, i: Int -> finish() }
                        .setNeutralButton("Cancel", null)
                        .show()
            }else{
                super.onBackPressed()
            }
        } else if (!TextUtils.isEmpty(binding.body.text.toString()) || !TextUtils.isEmpty(binding.toolbarEditText.text.toString())) {
            insertToDB()
            super.onBackPressed()
        }else{
            super.onBackPressed()
        }
    }
}

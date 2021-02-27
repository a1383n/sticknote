package ir.amirsobhan.sticknote

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.databinding.ActivityNoteBinding
import ir.amirsobhan.sticknote.helper.KeyboardManager
import ir.amirsobhan.sticknote.viewmodel.NoteViewModel

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private lateinit var keyboardManager: KeyboardManager
    private var note: Note? = null
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
        var json = intent.getStringExtra("json")
        if (json != null) {
            fromIntent = true
            note = Gson().fromJson(json,Note::class.java)

            binding.toolbarEditText.setText(note?.title)
            binding.body.setText(note?.text)
            binding.collapsingToolbarLayout.title = note?.title
        }
    }

    private fun setupAppBar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        if (binding.toolbarEditText.hint == getString(R.string.untitled) && TextUtils.isEmpty(binding.toolbarEditText.text.toString())) binding.collapsingToolbarLayout.title =
            getString(R.string.untitled)
        else binding.collapsingToolbarLayout.title = binding.toolbarEditText.text.toString()

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.save -> onBackPressed()
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

    private fun saveToDB() {
        if (fromIntent) return

        var noteViewModel: NoteViewModel =
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                .create(NoteViewModel::class.java)
        noteViewModel.insert(Note(null, binding.toolbarEditText.text.toString(), binding.body.text.toString(), System.currentTimeMillis()))

        Toast.makeText(this, "Your note saved successfully", Toast.LENGTH_LONG).show()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (!TextUtils.isEmpty(binding.body.text.toString())) {
            //     if (binding.toolbarEditText.hint == getString(R.string.untitled)) binding.toolbarEditText.text = getString(R.string.untitled)
            saveToDB()
        }
    }
}
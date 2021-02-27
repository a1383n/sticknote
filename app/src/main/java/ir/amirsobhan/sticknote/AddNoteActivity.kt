package ir.amirsobhan.sticknote

import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.databinding.ActivityAddNoteBinding
import ir.amirsobhan.sticknote.helper.KeyboardManager
import ir.amirsobhan.sticknote.viewmodel.NoteViewModel
import kotlinx.android.synthetic.main.activity_main.*

class AddNoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var keyboardManager: KeyboardManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        keyboardManager = KeyboardManager(this)

        setupAppBar()
        setEditText()
    }


    private fun setupAppBar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        if (binding.toolbarEditText.hint == getString(R.string.untitled)) binding.collapsingToolbarLayout.title =
            getString(R.string.untitled)
        else binding.collapsingToolbarLayout.title = binding.toolbarEditText.text.toString()

        binding.toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
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
        var noteViewModel: NoteViewModel =
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                .create(NoteViewModel::class.java)
        noteViewModel.insert(
            Note(
                null,
                binding.toolbarEditText.text.toString(),
                binding.body.text.toString(),
                System.currentTimeMillis()
            )
        )

    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (!TextUtils.isEmpty(binding.body.text.toString())) {
       //     if (binding.toolbarEditText.hint == getString(R.string.untitled)) binding.toolbarEditText.text = getString(R.string.untitled)
            saveToDB()
            Toast.makeText(this, "Your note saved successfully", Toast.LENGTH_LONG).show()
        }
    }
}
package ir.amirsobhan.sticknote.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import ir.amirsobhan.sticknote.NoteActivity
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.databinding.RowNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter(val context: Context?) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    var noteList: List<Note> = listOf()

    class ViewHolder(val binding: RowNoteBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.title.text = note.title;
            binding.body.text = note.text;
            binding.time.text = getDateTime(note.timestamp)

            binding.root.setOnClickListener {
                var intent = Intent(context, NoteActivity::class.java)
                intent.putExtra("json", Gson().toJson(note))
                context.startActivity(intent)
            }

        }

        fun getDateTime(long: Long): String? {
            try {
                val sdf = SimpleDateFormat("E h:m a")
                val netDate = Date(long)
                return sdf.format(netDate)
            } catch (e: Exception) {
                return e.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        RowNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false),context.let { context -> parent.context }
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        noteList.get(position).also { holder.bind(it) }
    }

    override fun getItemCount() = noteList.size
}
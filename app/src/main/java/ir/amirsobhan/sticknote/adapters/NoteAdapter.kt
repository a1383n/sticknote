package ir.amirsobhan.sticknote.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.databinding.RowNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    var noteList: List<Note> = listOf()

    class ViewHolder(val binding: RowNoteBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note){
            binding.title.text = note.title;
            binding.body.text = note.text;
            binding.time.text = getDateTime(note.timestamp)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(RowNoteBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        noteList.get(position).also { holder.bind(it) }
    }

    override fun getItemCount() = noteList.size
}
package ir.amirsobhan.sticknote.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.databinding.RowNoteBinding

class NoteAdapter(var noteList: List<Note>) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    class ViewHolder(rowNoteBinding: RowNoteBinding) : RecyclerView.ViewHolder(rowNoteBinding.root) {
        fun bind(note: Note){

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(RowNoteBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(noteList[position])
    }

    override fun getItemCount() = noteList.size
}
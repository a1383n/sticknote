package ir.amirsobhan.sticknote.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.adapters.NoteAdapter
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.databinding.FragmentCloudBinding
import ir.amirsobhan.sticknote.databinding.FragmentNotesBinding

class NotesFragment : Fragment() {
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentNotesBinding.inflate(layoutInflater,container,false)


        binding.recyclerView.adapter = NoteAdapter(listOf(
            Note(1,"Summer Fun","W",System.currentTimeMillis()),
            Note(1,"UX Basics","U",System.currentTimeMillis()),
            Note(1,"Family","F",System.currentTimeMillis())))

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
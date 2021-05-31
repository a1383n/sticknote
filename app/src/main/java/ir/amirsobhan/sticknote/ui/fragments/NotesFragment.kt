package ir.amirsobhan.sticknote.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import ir.amirsobhan.sticknote.adapters.NoteAdapter
import ir.amirsobhan.sticknote.databinding.FragmentNotesBinding
import ir.amirsobhan.sticknote.viewmodel.NoteViewModel
import org.koin.android.ext.android.inject

class NotesFragment : Fragment() {
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter : NoteAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentNotesBinding.inflate(layoutInflater,container,false)

        adapter = NoteAdapter(context)

        val model : NoteViewModel by inject()

        model.notes.observe(viewLifecycleOwner, Observer {
            adapter.noteList = it.toMutableList()
            adapter.notifyDataSetChanged()
        })
        binding.recyclerView.adapter = adapter

        binding.recyclerView.layoutManager?.isAutoMeasureEnabled = true
        binding.recyclerView.isNestedScrollingEnabled = false

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
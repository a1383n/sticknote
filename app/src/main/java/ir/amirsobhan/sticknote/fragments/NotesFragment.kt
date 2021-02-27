package ir.amirsobhan.sticknote.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ir.amirsobhan.sticknote.adapters.NoteAdapter
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.databinding.FragmentNotesBinding
import ir.amirsobhan.sticknote.viewmodel.NoteViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_notes.*

class NotesFragment : Fragment() {
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter : NoteAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentNotesBinding.inflate(layoutInflater,container,false)

        adapter = NoteAdapter(context)

        val model : NoteViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application).create(NoteViewModel::class.java)
        model.notes.observe(this, Observer {
            adapter.noteList = it
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
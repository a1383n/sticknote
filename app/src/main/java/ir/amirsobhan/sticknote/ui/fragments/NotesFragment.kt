package ir.amirsobhan.sticknote.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.adapters.NoteAdapter
import ir.amirsobhan.sticknote.databinding.FragmentNotesBinding
import ir.amirsobhan.sticknote.viewmodel.NoteViewModel
import org.koin.android.ext.android.inject

class NotesFragment : Fragment() {
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private val viewModel : NoteViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        _binding = FragmentNotesBinding.inflate(layoutInflater,container,false)
        viewModel.adapter = NoteAdapter(requireContext(),viewModel.selectedNote)

        binding.recyclerView.adapter = viewModel.adapter

        //Observe notes in local database
        viewModel.notes.observe(viewLifecycleOwner, {
            viewModel.adapter?.setList(it)
        })

        //Observe for select note to change toolbar
        viewModel.selectedNote.observe(viewLifecycleOwner, {
            if (viewModel.actionMode == null && it.size > 0){
                viewModel.actionMode = binding.toolbar.startActionMode(viewModel.actionModeCallback(requireContext()))
                viewModel.actionMode?.title = getString(R.string.item_selected,it.size)
            }else{
                if (viewModel.actionMode != null && it.size == 0){
                    viewModel.adapter?.unCheckAll()
                    viewModel.actionMode?.finish()
                    viewModel.actionMode = null
                }else{
                    viewModel.actionMode?.title = getString(R.string.item_selected,it.size)
                }
            }
        })


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.actionMode?.finish()
    }
}
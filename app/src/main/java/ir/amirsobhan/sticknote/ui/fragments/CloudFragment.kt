  package ir.amirsobhan.sticknote.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.databinding.FragmentCloudBinding
import ir.amirsobhan.sticknote.repositories.NoteRepository
import ir.amirsobhan.sticknote.ui.activity.AuthActivity
import ir.amirsobhan.sticknote.viewmodel.CloudViewModel
import ir.amirsobhan.sticknote.worker.AutoSync
import org.koin.android.ext.android.inject
import java.util.*

  class CloudFragment : Fragment(){
    private val TAG = "CloudFragment"
    private var _binding : FragmentCloudBinding? = null
    private val binding get() = _binding!!
    private val viewModel : CloudViewModel by inject()
    private val auth : FirebaseAuth = Firebase.auth
    private val isUserSignedIn :
            Boolean get() = auth.currentUser != null
    private val noteRepository : NoteRepository by inject()
    val workManager : WorkManager by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentCloudBinding.inflate(layoutInflater, container, false)

        updateUI()

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode == Activity.RESULT_OK){
            Toast.makeText(context,"Welcome, Sync in progress...",Toast.LENGTH_SHORT).show()

            workManager.enqueue(AutoSync.Factory(AutoSync.SYNC))

            updateUI()
        }
    }

    private fun updateUI(){
        if (isUserSignedIn){
            binding.signInCard.visibility = View.GONE
            binding.cloudCard.visibility = View.VISIBLE
            binding.lastSync.text = viewModel.getLastSyncDate()

            binding.syncButtom.setOnClickListener {
                binding.syncButtom.isEnabled = false
                var id : UUID
                workManager.enqueue(AutoSync.Factory(AutoSync.SYNC).also { id = it.id })
                workManager.getWorkInfoByIdLiveData(id)
                    .observe(viewLifecycleOwner, Observer {
                        if (it.state == WorkInfo.State.SUCCEEDED){
                            binding.syncButtom.isEnabled = true
                            binding.lastSync.text = viewModel.getLastSyncDate()
                        }
                    })
            }

        }else{
            binding.signInCard.visibility = View.VISIBLE
            binding.cloudCard.visibility = View.GONE
            binding.signInBtn.setOnClickListener {
                startActivityForResult(Intent(activity,AuthActivity::class.java),10)
            }
        }
    }
}
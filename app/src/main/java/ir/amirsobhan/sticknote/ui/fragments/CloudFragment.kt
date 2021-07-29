  package ir.amirsobhan.sticknote.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.work.WorkInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.FragmentCloudBinding
import ir.amirsobhan.sticknote.ui.activity.AuthActivity
import ir.amirsobhan.sticknote.viewmodel.CloudViewModel
import org.koin.android.ext.android.inject

  class CloudFragment : Fragment(){
    private var _binding : FragmentCloudBinding? = null
    private val binding get() = _binding!!
    private val viewModel : CloudViewModel by inject()
    private val auth : FirebaseAuth = Firebase.auth
    private val isUserSignedIn get() = auth.currentUser != null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        _binding = FragmentCloudBinding.inflate(layoutInflater, container, false)

        updateUI()

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CloudViewModel.LOGIN_REQ_CODE && resultCode == Activity.RESULT_OK){
            viewModel.handleActivityResult()
            Toast.makeText(context, R.string.welcome_signin, Toast.LENGTH_SHORT).show()
            updateUI()
        }
    }

    private fun updateUI(){
        if (isUserSignedIn){
            binding.signInCard.visibility = View.GONE
            binding.cloudCard.visibility = View.VISIBLE
            binding.lastSync.text = getString(R.string.last_sync,viewModel.getLastSyncDate())

            binding.syncButtom.setOnClickListener {
                binding.syncButtom.isEnabled = false
                viewModel.startSyncWork()
                    .observe(viewLifecycleOwner, {
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
                startActivityForResult(Intent(activity,AuthActivity::class.java),CloudViewModel.LOGIN_REQ_CODE)
            }
        }
    }
}
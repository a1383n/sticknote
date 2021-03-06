package ir.amirsobhan.sticknote.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.solver.state.State
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.work.*
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import ir.amirsobhan.sticknote.databinding.FragmentCloudBinding
import ir.amirsobhan.sticknote.repositories.NoteRepository
import ir.amirsobhan.sticknote.viewmodel.CloudViewModel
import ir.amirsobhan.sticknote.worker.SyncLocalDBwithRemote
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit


class CloudFragment : Fragment(){
    private val TAG = "CloudFragment"
    private var _binding : FragmentCloudBinding? = null
    private val binding get() = _binding!!
    private val viewModel : CloudViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentCloudBinding.inflate(layoutInflater, container, false)

        updateUI()

        return binding.root
    }

    fun updateUI(){
        if (!viewModel.isUserSignIn()){
            binding.signInCard.visibility = View.VISIBLE
            binding.cloudCard.visibility = View.GONE
            binding.signInBtn.setOnClickListener { startActivityForResult(viewModel.createAuthUIIntent(),CloudViewModel.ACTIVITY_RESULT_REQUEST_CODE) }
        }else{
            binding.signInCard.visibility = View.GONE
            binding.cloudCard.visibility = View.VISIBLE
            binding.syncButtom.setOnClickListener { sync() }
        }
    }

    fun sync(){
        viewModel.reps.getAll().observe(viewLifecycleOwner, Observer {

            binding.syncButtom.isEnabled = false

            val workReq = PeriodicWorkRequestBuilder<SyncLocalDBwithRemote>(1,TimeUnit.HOURS)
                .setInputData(workDataOf(
                    "json" to Gson().toJson(it)
                ))
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()

            WorkManager.getInstance(requireContext())
                .enqueue(workReq)

            WorkManager.getInstance(requireContext())
                .getWorkInfoByIdLiveData(workReq.id)
                .observe(viewLifecycleOwner, Observer {
                    if (it != null && it.state.isFinished){
                       binding.syncButtom.isEnabled = true
                    }
                })

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CloudViewModel.ACTIVITY_RESULT_REQUEST_CODE){
            val response = IdpResponse.fromResultIntent(data)

            if (requestCode == Activity.RESULT_OK){
                // Successfully signed in
                updateUI()
            }else{
                if (response != null){
                    Toast.makeText(context,response.error?.message,Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
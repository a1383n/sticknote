package ir.amirsobhan.sticknote.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.database.AppDatabase
import ir.amirsobhan.sticknote.databinding.FragmentCloudBinding
import ir.amirsobhan.sticknote.repositories.NoteRepository
import ir.amirsobhan.sticknote.viewmodel.CloudViewModel
import ir.amirsobhan.sticknote.viewmodel.NoteViewModel
import org.koin.android.ext.android.inject


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
        val db = Firebase.firestore
        val reps : NoteRepository by inject()

        reps.getAll().observe(viewLifecycleOwner, Observer {
            db.collection("users").document(viewModel.user.uid)
                .set(hashMapOf("notes" to Gson().toJson(it), "timestamp" to System.currentTimeMillis()))
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
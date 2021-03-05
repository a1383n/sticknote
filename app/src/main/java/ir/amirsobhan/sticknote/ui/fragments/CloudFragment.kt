package ir.amirsobhan.sticknote.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.databinding.FragmentCloudBinding
import ir.amirsobhan.sticknote.viewmodel.CloudViewModel
import org.koin.android.ext.android.inject


class CloudFragment : Fragment(){
    private val TAG = "CloudFragment"
    private var _binding : FragmentCloudBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentCloudBinding.inflate(layoutInflater, container, false)


        binding.signInBtn.setOnClickListener {
            startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build(),AuthUI.IdpConfig.EmailBuilder().build()))
                .setDefaultProvider(AuthUI.IdpConfig.GoogleBuilder().build())
                .build(),
            14)
        }

        return binding.root
    }
}
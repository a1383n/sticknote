package ir.amirsobhan.sticknote.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import ir.amirsobhan.sticknote.databinding.FragmentCloudBinding
import ir.amirsobhan.sticknote.viewmodel.CloudViewModel
import org.koin.android.ext.android.inject


class CloudFragment : Fragment(){
    private val TAG = "CloudFragment"
    private var _binding : FragmentCloudBinding? = null
    private val binding get() = _binding!!

    val cloudViewModel : CloudViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentCloudBinding.inflate(layoutInflater, container, false)

        if (cloudViewModel.isLogin()){
            binding.signInCard.visibility = View.GONE
        }else{
            binding.signInBtn.setOnClickListener { startActivityForResult(cloudViewModel.gsc.signInIntent,cloudViewModel.GOOGLE_SIGN_IN_RC) }
        }

        return binding.root
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == cloudViewModel.GOOGLE_SIGN_IN_RC){
            cloudViewModel.handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data))
        }
    }

}
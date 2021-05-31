package ir.amirsobhan.sticknote.ui.fragments.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.FragmentLoginBinding
import ir.amirsobhan.sticknote.ui.activity.AuthActivity

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val TAG = "LoginFragment"
    private lateinit var auth : FirebaseAuth
    private lateinit var display_name : String

    val data : Intent = Intent()

    override fun onCreateView(inflater: LayoutInflater,  container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)

        init()

        binding.loginButton.setOnClickListener {
            if (formValidation()){
                auth.signInWithEmailAndPassword(binding.inputEmail.text.toString(),binding.inputPassword.text.toString())
                    .addOnSuccessListener {
                        activity?.setResult(Activity.RESULT_OK,data)
                        activity?.finish()
                    }
                    .addOnFailureListener {
                       binding.loginError.apply {
                           visibility = View.VISIBLE
                           text = it.message
                       }
                    }
            }
        }
        binding.googleSignInBtn.setOnClickListener { googleSignInRequest().also { binding.googleSignInBtn.isEnabled = false } }
        binding.resetPasswordButton.setOnClickListener { findNavController().navigate(R.id.action_loginFragment_to_restPasswordFragment, bundleOf("email" to binding.inputEmail.text.toString())) }

        return binding.root
    }

    fun init() {
        binding.registerButton.setOnClickListener { findNavController().navigate(R.id.action_loginFragment_to_registerFragment) }
        auth = Firebase.auth
    }

    fun formValidation(): Boolean {
        if (binding.inputEmail.text.isNullOrBlank()) {
            binding.emailLayout.error = "Email cannot be empty"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
            binding.emailLayout.error = "Enter a valid email."
            return false
        } else if(binding.inputPassword.text.isNullOrBlank()) {
            binding.emailLayout.isErrorEnabled = false
            binding.emailLayout.error = null

            binding.passwordLayout.error = "Password cannot be empty"
            return false
        } else {
            binding.passwordLayout.isErrorEnabled = false
            binding.passwordLayout.error = null
            return true
        }
    }

    fun googleSignInRequest(){
        val gso = GoogleSignInOptions.Builder()
            .requestIdToken("1064789206835-b6rnpf9adkfq5s29evctk067ce2opjai.apps.googleusercontent.com")
                .requestScopes(Scope("profile"))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireContext(),gso)

        startActivityForResult(googleSignInClient.signInIntent,15)
    }

    fun firebaseAuthWithGoogle(idToken : String){
        val credential = GoogleAuthProvider.getCredential(idToken,null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                        .setDisplayName(this.display_name)
                        .build()

                it.user.updateProfile(userProfileChangeRequest)


                data.putExtra("name",it.user.displayName)
                activity?.setResult(Activity.RESULT_OK,data)
                activity?.finish()
            }
            .addOnFailureListener {
                Log.d(TAG, "firebaseAuthWithGoogle: ${it.message}")
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 15){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                display_name = account.displayName!!
                firebaseAuthWithGoogle(account.idToken!!)
            }catch (e : ApiException){

            }
        }
    }
}
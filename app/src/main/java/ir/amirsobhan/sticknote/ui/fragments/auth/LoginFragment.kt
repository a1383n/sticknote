package ir.amirsobhan.sticknote.ui.fragments.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.FragmentLoginBinding
import ir.amirsobhan.sticknote.helper.AES
import org.koin.android.ext.android.inject

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val auth = Firebase.auth
    private lateinit var displayName : String
    private lateinit var accountId : String

    val data : Intent = Intent()

    override fun onCreateView(inflater: LayoutInflater,  container: ViewGroup?,savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)

        init()

        return binding.root
    }

    private fun init() {
        binding.loginButton.setOnClickListener { if (formValidation()){ loginWithEmail() } }
        binding.googleSignInBtn.setOnClickListener { googleSignInRequest() }
        binding.resetPasswordButton.setOnClickListener { navigateToResetPassword() }
        binding.registerButton.setOnClickListener { navigateToRegister() }
    }

    /**
     * Validate login form
     * @return The form is validate or not
     */
    private fun formValidation(): Boolean {
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

    /**
     * Request to login with email and password
     */
    private fun loginWithEmail() {
        auth.signInWithEmailAndPassword(binding.inputEmail.text.toString(),binding.inputPassword.text.toString())
            .addOnSuccessListener {
                Firebase.analytics.logEvent(FirebaseAnalytics.Event.LOGIN,null)
                Firebase.analytics.setUserProperty(FirebaseAnalytics.UserProperty.SIGN_UP_METHOD,"email")

                AES.buildSecret(binding.inputPassword.text.toString())

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

    /**
     * Request to signIn with google
     */
    private fun googleSignInRequest() {
        val gso : GoogleSignInOptions by inject()
        val googleSignInClient = GoogleSignIn.getClient(requireContext(),gso)
        binding.googleSignInBtn.isEnabled = false
        startActivityForResult(googleSignInClient.signInIntent,15)
    }

    private fun navigateToResetPassword() {
        findNavController()
            .navigate(R.id.action_loginFragment_to_restPasswordFragment, bundleOf("email" to binding.inputEmail.text.toString()))
    }

    private fun navigateToRegister() {
        findNavController()
            .navigate(R.id.action_loginFragment_to_registerFragment)
    }

    /**
     * Auth google credential with firebase
     * @param idToken The id token was google return to app
     */
    fun firebaseAuthWithGoogle(idToken : String) {
        val credential = GoogleAuthProvider.getCredential(idToken,null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                        .setDisplayName(this.displayName)
                        .build()

                it.user?.updateProfile(userProfileChangeRequest)

                Firebase.analytics.logEvent(FirebaseAnalytics.Event.LOGIN,null)
                Firebase.analytics.setUserProperty(FirebaseAnalytics.UserProperty.SIGN_UP_METHOD,"google")
                data.putExtra("name",it.user?.displayName)
                activity?.setResult(Activity.RESULT_OK,data)
                activity?.finish()
            }
            .addOnFailureListener {
                if (it !is FirebaseException) {
                    Snackbar.make(requireView(), it.toString(), Snackbar.LENGTH_SHORT).show()
                }else{
                    Snackbar.make(requireView(),getString(R.string.error_occurred,getString(R.string.error_403_google)),Snackbar.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Handel google signIn action result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 15){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                displayName = account.displayName!!
                accountId = account.id!!
                firebaseAuthWithGoogle(account.idToken!!)
            }catch (e : ApiException){
                Firebase.crashlytics.recordException(e)
                Snackbar.make(requireView(),getString(R.string.error_occurred_during,"the google signIn.code ${e.message}"),Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
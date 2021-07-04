package ir.amirsobhan.sticknote.ui.fragments.auth

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.SheetEmailChangeBinding
import ir.amirsobhan.sticknote.helper.KeyboardManager
import ir.amirsobhan.sticknote.mainThread
import org.koin.android.ext.android.inject

class EmailChangeFragment(val newEmail: String) : BottomSheetDialogFragment() {
    private val TAG = "EmailChangeFragment"
    var _binding: SheetEmailChangeBinding? = null
    val binding get() = _binding!!
    val auth = Firebase.auth
    val user = auth.currentUser!!
    val providerType get() = findProviderType(user)
    val isEmailChange = MutableLiveData<Boolean>(false)
    val isEmailChangeError = MutableLiveData<Pair<Boolean,String?>>(Pair(false,null))
    val gso : GoogleSignInOptions by inject()
    val gsc get() = GoogleSignIn.getClient(requireContext(),gso)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = SheetEmailChangeBinding.inflate(layoutInflater, container, false)
        showLayout(providerType)
        return binding.root
    }


    private fun showLayout(type: ProviderType) {
        when (type) {
            ProviderType.EMAIL -> {
                binding.constraintLayout.isVisible = true
                binding.progressBar.isVisible = false
                binding.passAuth.isVisible = true

                binding.passwordSubmit.setOnClickListener { getEmailProviderCredential() }
            }
            ProviderType.GOOGLE -> {
                binding.constraintLayout.isVisible = true
                binding.progressBar.isVisible = false
                binding.googleAuth.isVisible = true

                binding.emailChangeDes.setText(R.string.email_change_google_verify)
                binding.googleSignInBtn.setOnClickListener { getGoogleProviderCredential() }
            }
        }
    }

    private fun loading(bool : Boolean){
        binding.constraintLayout.isVisible = !bool
        binding.progressBar.isVisible = bool
    }

    private fun getEmailProviderCredential() {
        binding.passwordLay.error = null
        if (!binding.passwordInp.text.isNullOrBlank()) {
            Log.d(TAG, "getEmailProviderCredential: ")
            reAuthentication(EmailAuthProvider.getCredential(user.email.toString(), binding.passwordInp.text.toString()))
        } else {
            binding.passwordLay.error = "Password cannot be empty"
        }
    }

    private fun getGoogleProviderCredential(){
        loading(true)
        startActivityForResult(gsc.signInIntent,10)
    }

    private fun reAuthentication(credential: AuthCredential) {
        user.reauthenticateAndRetrieveData(credential)
                .addOnSuccessListener { changeEmail(it.user!!).also { Log.d(TAG, "reAuthentication: $it") } }
                .addOnFailureListener { binding.passwordLay.error = it.message.also { Log.d(TAG, "reAuthentication: $it") } }
    }

    private fun changeEmail(user: FirebaseUser) {
        user.verifyBeforeUpdateEmail(newEmail)
                .addOnSuccessListener {
                    Log.d(TAG, "changeEmail: $it")
                    Toast.makeText(context,R.string.email_change_link_send,Toast.LENGTH_LONG).show()
                    isEmailChange.postValue(true)
                    dismiss()
                    gsc.revokeAccess()
                    user.unlink(GoogleAuthProvider.PROVIDER_ID)
                }
                .addOnFailureListener {
                    Log.d(TAG, "changeEmail: ${it.message}")
                    isEmailChange.postValue(false)
                    isEmailChangeError.postValue(Pair(true,it.message))
                    dismiss()
                }
    }

    private fun findProviderType(user: FirebaseUser): ProviderType {
        val providerList = mutableListOf<String>()
        user.providerData.forEach {
            providerList.add(it.providerId)
        }

        Log.d(TAG, "findProviderType: $providerList")

        if (providerList.contains(GoogleAuthProvider.PROVIDER_ID)){
            return ProviderType.GOOGLE
        }else if (providerList.contains(EmailAuthProvider.PROVIDER_ID)){
            return ProviderType.EMAIL
        }else{
            return ProviderType.NULL
        }
    }

    enum class ProviderType {
        EMAIL, GOOGLE, NULL
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            mainThread(requireContext()).execute {
                val bottomSheet = (dialog as? BottomSheetDialog)?.findViewById<View>(R.id.design_bottom_sheet) as? FrameLayout
                bottomSheet?.let {
                    BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
        return dialog
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                reAuthentication(GoogleAuthProvider.getCredential(account.idToken,null))
            }catch (e: ApiException){
                loading(false)
                Log.e(TAG, "onActivityResult: ${e.message}", e)
            }
        }
    }

}


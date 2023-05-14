package ir.amirsobhan.sticknote.ui.fragments.auth

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.SheetEmailChangeBinding
import ir.amirsobhan.sticknote.mainThread
import org.koin.android.ext.android.inject

class EmailChangeFragment(private val newEmail: String) : BottomSheetDialogFragment() {
    private var _binding: SheetEmailChangeBinding? = null
    val binding get() = _binding!!
    val auth = Firebase.auth
    private val user = auth.currentUser!!
    private val providerType get() = findProviderType(user)
    val verificationStatus = MutableLiveData(EmailVerificationStatus.INIT)
    var verificationError : String? = null
    private val gso : GoogleSignInOptions by inject()
    private val gsc: GoogleSignInClient get() = GoogleSignIn.getClient(requireContext(),gso)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SheetEmailChangeBinding.inflate(layoutInflater, container, false)
        showLayout(providerType)
        return binding.root
    }

    enum class EmailVerificationStatus{
        COMPLETED,
        ERROR,
        WAITING_USER_AUTH,
        CANCELED,
        INIT
    }

    /**
     * Show layout for different provider type
     */
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
            else -> ProviderType.NULL
        }

        verificationStatus.postValue(EmailVerificationStatus.WAITING_USER_AUTH)
    }

    /**
     * Show loading or tick icon on toolbar
     * @param bool Loading show or not
     */
    private fun loading(bool : Boolean){
        binding.constraintLayout.isVisible = !bool
        binding.progressBar.isVisible = bool
    }

    private fun getEmailProviderCredential() {
        binding.passwordLay.error = null
        if (!binding.passwordInp.text.isNullOrBlank()) {
            reAuthentication(EmailAuthProvider.getCredential(user.email.toString(), binding.passwordInp.text.toString()))
        } else {
            binding.passwordLay.error = getString(R.string.password_empty)
        }
    }

    private fun getGoogleProviderCredential(){
        loading(true)
        startActivityForResult(gsc.signInIntent,10)
    }

    /**
     * Re authenticate and retrieve data
     * @param credential The credential to authenticate user
     */
    private fun reAuthentication(credential: AuthCredential) {
        user.reauthenticateAndRetrieveData(credential)
                .addOnSuccessListener { changeEmail(it.user!!) }
                .addOnFailureListener { binding.passwordLay.error = it.message }
    }

    /**
     * Request for email change
     * @param user The FirebaseUser to request to change email
     */
    private fun changeEmail(user: FirebaseUser) {
        user.verifyBeforeUpdateEmail(newEmail)
                .addOnSuccessListener {
                    Toast.makeText(context,R.string.email_change_link_send,Toast.LENGTH_LONG).show()
                    gsc.revokeAccess()
                    user.unlink(GoogleAuthProvider.PROVIDER_ID)
                    verificationStatus.postValue(EmailVerificationStatus.COMPLETED)
                }
                .addOnFailureListener {
                    verificationError = it.message
                    verificationStatus.postValue(EmailVerificationStatus.ERROR)
                    dismiss()
                }
    }

    /**
     * Find provider type of FirebaseUser
     * @param user The firebase user to find all provider type
     * @return The best ProviderType
     */
    private fun findProviderType(user: FirebaseUser): ProviderType {
        val providerList = mutableListOf<String>()
        user.providerData.forEach {
            providerList.add(it.providerId)
        }

        return when {
            providerList.contains(GoogleAuthProvider.PROVIDER_ID) -> { ProviderType.GOOGLE }
            providerList.contains(EmailAuthProvider.PROVIDER_ID) -> { ProviderType.EMAIL }
            else -> { ProviderType.NULL }
        }
    }

    enum class ProviderType {
        EMAIL, GOOGLE, NULL
    }

    /**
     * This override for show bottom sheet in real size
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            mainThread(requireContext()).execute {
                val bottomSheet = (dialog as? BottomSheetDialog)?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as? FrameLayout
                bottomSheet?.let {
                    BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
        return dialog
    }

    /**
     * When dialog dismiss post some value ro profileFragment
     */
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        verificationStatus.postValue(EmailVerificationStatus.CANCELED)
    }

    /**
     * Handel google signIn result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                reAuthentication(GoogleAuthProvider.getCredential(account.idToken,null))
            }catch (e: ApiException){
                loading(false)
                Firebase.crashlytics.recordException(e)
            }
        }
    }

}


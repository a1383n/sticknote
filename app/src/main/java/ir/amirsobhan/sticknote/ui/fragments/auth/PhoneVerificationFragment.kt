package ir.amirsobhan.sticknote.ui.fragments.auth

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.databinding.SheetPhoneVerifyBinding
import java.util.concurrent.TimeUnit

class PhoneVerificationFragment(val phoneNumber : String) : BottomSheetDialogFragment() {
    val TAG = "PhoneAuth"
    private var _binding: SheetPhoneVerifyBinding? = null
    val binding get() = _binding!!
    val auth = Firebase.auth
    val isPhoneVerified : MutableLiveData<Boolean> = MutableLiveData(false)
    var verificationID : String? = null
    var resendingToken : PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = SheetPhoneVerifyBinding.inflate(layoutInflater, container, false)

        binding.verifyCodeInp.addTextChangedListener { textWatcher(it!!) }

        phoneVerifyRequest(phoneNumber)

        return binding.root
    }

    private fun startCountDownTimer(timeOut : Long) {
        object : CountDownTimer(timeOut, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.countdownTimer.text = String.format(
                        "%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)))
            }

            override fun onFinish() {
                binding.countdownTimer.isVisible = false
                binding.resendCode.isVisible = true
            }
        }.start()
    }

    private fun phoneVerifyRequest(phoneNumber: String){

        Log.d(TAG, "phoneVerifyRequest: $phoneNumber")
        
        val phoneAuthOptions = PhoneAuthOptions.Builder(auth)
                .setPhoneNumber(phoneNumber)
                .setActivity(requireActivity())
                .setTimeout(120,TimeUnit.SECONDS)
                .setCallbacks(getVerificationCallback())
                .build()

        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)

        isCancelable = false
    }

    private fun getVerificationCallback(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        return object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {

                binding.progressBar.isVisible = false
                binding.constraintLayout.isVisible = true

                super.onCodeSent(p0, p1)
                verificationID = p0
                resendingToken = p1

                startCountDownTimer(TimeUnit.SECONDS.toMillis(120))

                Log.d(TAG, "onCodeSent: $p0")
            }

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$p0")
                verifyCredential(p0)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.e(TAG, "onVerificationFailed", p0)

                if (p0 is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (p0 is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }
            }

        }
    }

    private fun textWatcher(editable: Editable) {
        if (editable.length == 6) {
            if (verificationID != null){
                verifyCredential(PhoneAuthProvider.getCredential(verificationID!!,editable.toString()))
            }
        }
    }

    private fun verifyCredential(phoneAuthCredential: PhoneAuthCredential) {
        binding.verifyCodeLy.error = null
        auth.currentUser?.updatePhoneNumber(phoneAuthCredential)
                ?.addOnSuccessListener { isPhoneVerified.postValue(true).also { dismiss() } }
                ?.addOnFailureListener {
                    Log.e(TAG, "verifyCredential: ", it)
                    binding.verifyCodeLy.error = it.message
                }
    }
}
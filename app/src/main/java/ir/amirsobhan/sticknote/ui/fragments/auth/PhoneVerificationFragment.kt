package ir.amirsobhan.sticknote.ui.fragments.auth

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.SheetPhoneVerifyBinding
import ir.amirsobhan.sticknote.mainThread
import java.util.concurrent.TimeUnit

class PhoneVerificationFragment(private val phoneNumber : String) : BottomSheetDialogFragment() {
    private var _binding: SheetPhoneVerifyBinding? = null
    val binding get() = _binding!!
    val auth = Firebase.auth
    var verificationError : String? = null
    val verificationStatus = MutableLiveData(PhoneVerificationStatus.INIT)
    var verificationID : String? = null
    var resendingToken : PhoneAuthProvider.ForceResendingToken? = null
    val countDownTimer = object : CountDownTimer(TimeUnit.SECONDS.toMillis(120),1000){
        override fun onTick(millisUntilFinished: Long) {
            binding.countdownTimer.text = DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished))
        }

        override fun onFinish() {
            binding.resendButton.isVisible = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SheetPhoneVerifyBinding.inflate(layoutInflater, container, false)

        init()
        phoneVerifyRequest(phoneNumber)

        return binding.root
    }

    enum class PhoneVerificationStatus {
        COMPLETED,
        ERROR,
        TIME_OUT,
        CODE_RECEIVED,
        WAITING_FOR_CODE,
        REQUEST_FOR_CODE,
        CANCELED,
        INIT
    }

    private fun init(){
        binding.verifyCodeInp.addTextChangedListener { textWatcher(it!!) }
        binding.resendButton.setOnClickListener { resendCode() }
    }

    private fun phoneVerifyRequest(phoneNumber: String, resendingToken: PhoneAuthProvider.ForceResendingToken? = null){
        val phoneAuthOptions = PhoneAuthOptions.Builder(auth)
                .setPhoneNumber(phoneNumber)
                .setActivity(requireActivity())
                .setTimeout(120,TimeUnit.SECONDS)
                .setCallbacks(getVerificationCallback())

                if (resendingToken != null) {
                    phoneAuthOptions.setForceResendingToken(resendingToken)
                }

        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions.build())
        verificationStatus.postValue(PhoneVerificationStatus.REQUEST_FOR_CODE)
    }

    private fun resendCode(){
        binding.resendButton.isVisible = false
        phoneVerifyRequest(phoneNumber,resendingToken)
    }

    private fun getVerificationCallback(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        return object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                verificationStatus.postValue(PhoneVerificationStatus.WAITING_FOR_CODE)

                binding.progressBar.isVisible = false
                binding.constraintLayout.isVisible = true
                verificationID = p0
                resendingToken = p1

                countDownTimer.start()
            }

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                verifyCredential(p0)
                verificationStatus.postValue(PhoneVerificationStatus.CODE_RECEIVED)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                verificationError = p0.message
                verificationStatus.postValue(PhoneVerificationStatus.ERROR)
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String) {
                super.onCodeAutoRetrievalTimeOut(p0)
                verificationStatus.postValue(PhoneVerificationStatus.TIME_OUT)
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
                ?.addOnSuccessListener {
                    verificationStatus.postValue(PhoneVerificationStatus.COMPLETED)
                }
                ?.addOnFailureListener {
                    verificationError = it.message
                    verificationStatus.postValue(PhoneVerificationStatus.ERROR)
                }
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        verificationStatus.postValue(PhoneVerificationStatus.CANCELED)
    }
}
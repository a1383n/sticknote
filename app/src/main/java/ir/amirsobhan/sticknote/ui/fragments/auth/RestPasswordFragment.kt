package ir.amirsobhan.sticknote.ui.fragments.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.FragmentResetPasswordBinding

class RestPasswordFragment : Fragment() {
    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    val args: RestPasswordFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentResetPasswordBinding.inflate(layoutInflater, container, false)
        init()


        binding.resetPasswordButton.setOnClickListener { btn ->
            binding.resetPasswordButton.isEnabled = false
            if (inputValidation()) {
                auth.sendPasswordResetEmail(binding.textInputEditText.text.toString())
                        .addOnSuccessListener {
                            Toast.makeText(context, R.string.reset_password_email_sent, Toast.LENGTH_LONG).show()
                            findNavController().navigateUp()
                        }
                        .addOnFailureListener {
                            binding.resetError.apply {
                                binding.resetPasswordButton.isEnabled = true
                                visibility = View.VISIBLE
                                if (it is FirebaseException) {
                                    text = it.toString()
                                } else {
                                    text = it.toString()
                                }
                            }
                        }
            }
        }

        return binding.root
    }

    private fun init() {
        auth = Firebase.auth
        if (Patterns.EMAIL_ADDRESS.matcher(args.email.toString()).matches()) {
            binding.textInputEditText.setText(args.email)
        }
    }

    fun inputValidation(): Boolean {
        binding.textInputLayout.isErrorEnabled = false
        binding.textInputLayout.error = null

        if (binding.textInputEditText.text.isNullOrBlank()) {
            binding.textInputLayout.error = "Email cannot be empty"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.textInputEditText.text.toString()).matches()) {
            binding.textInputLayout.error = "Enter a valid email."
            return false
        } else {
            return true
        }
    }
}
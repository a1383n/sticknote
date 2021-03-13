package ir.amirsobhan.sticknote.ui.fragments.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater,  container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)

        init()

        return binding.root
    }

    fun init() {
        binding.registerButton.setOnClickListener { findNavController().navigate(R.id.action_loginFragment_to_registerFragment) }
        binding.loginButton.setOnClickListener {
            if (formValidation()) {

            }
        }
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
}
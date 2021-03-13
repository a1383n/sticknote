package ir.amirsobhan.sticknote.ui.fragments.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.Fragment
import ir.amirsobhan.sticknote.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {
    private var _binding : FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRegisterBinding.inflate(layoutInflater,container,false)


        init()


        return binding.root
    }

    fun init(){
        binding.registerButton.setOnClickListener {
            if (formValidation()){

            }
        }
    }

    fun formValidation() : Boolean{
        // Null all error first
        binding.nameInputLayout.error = null
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
        binding.passwordConInputLayout.error = null

        //Disable error
        binding.nameInputLayout.isErrorEnabled = false
        binding.emailInputLayout.isErrorEnabled = false
        binding.passwordInputLayout.isErrorEnabled = false
        binding.passwordConInputLayout.isErrorEnabled = false

        if (binding.inputName.text.isNullOrBlank()){
            binding.nameInputLayout.error = "Name cannot be empty"
            return false
        }else if (binding.inputEmail.text.isNullOrBlank()){
            binding.emailInputLayout.error = "Email cannot be empty"
            return false
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()){
            binding.emailInputLayout.error = "Enter a valid email."
            return false
        }else if (binding.passwordInput.text.isNullOrBlank()){
            binding.passwordInputLayout.error = "Password cannot be empty"
            return false
        }else if (binding.passwordInput.text.toString().length < 8){
            binding.passwordInputLayout.error = "Password length should 8 or above"
            return false
        }else if (binding.passwordInput.text.toString() != binding.passwordConInput.text.toString()){
            binding.passwordConInputLayout.error = "password confirmation doesn't match password"
            return false
        }else{
            return true
        }
    }
}
package ir.amirsobhan.sticknote.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.FragmentProfileBinding
import ir.amirsobhan.sticknote.helper.KeyboardManager
import ir.amirsobhan.sticknote.ui.fragments.auth.EmailChangeFragment
import ir.amirsobhan.sticknote.ui.fragments.auth.PhoneVerificationFragment
import java.io.ByteArrayOutputStream


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    val auth = Firebase.auth
    private val user = auth.currentUser!!
    private val keyboardManager get() = lazy { KeyboardManager(requireContext()) }
    private var profileUri = user.photoUrl
    private val storage = Firebase.storage
    private val inputPhoneNumber get() = binding.proPhoneInput.text.toString().filter { !it.isWhitespace() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)

        setupToolbar()
        fillInputs()

        // Set onClickListener for profile picture
        binding.profileImage.setOnClickListener { changeProfilePicture() }

        return binding.root
    }

    private fun setupToolbar() {
        // When navigationIcon on toolbar clicked hide keyboard & navigate up
        binding.toolbar.setNavigationOnClickListener {
            keyboardManager.value.closeKeyboard(requireView())
            findNavController().navigateUp()
        }

        // When click on save button on toolbar
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.profile_menu_save -> if (verifyInputs()) { startRouting() }
            }
            true
        }
    }

    /**
     * Show loading progressbar instead of save icon
     * @param bool The param if it true show loading on toolbar if false show save icon
     */
    private fun loading(bool: Boolean) {
        if (bool) {
            binding.toolbar.menu[0].actionView = ProgressBar(requireContext())
        } else {
            binding.toolbar.menu[0].actionView = null
        }
    }

    /**
     * Start routeing to verify email, phone & password
     */
    private fun startRouting(){
        loading(true)

        if (isPasswordChangeRequest()){
            if (verifyPasswordsInputs()){
                user.updatePassword(binding.proPassInput.text.toString())
            }
        }

        if (isPhoneChangeRequest() && !isEmailChangeRequest()){
            startPhoneVerification { exit() }

        }else if(!isPhoneChangeRequest() && isEmailChangeRequest()){
            startEmailVerification { exit() }

        }else if (isPhoneChangeRequest() && isEmailChangeRequest()){
            startPhoneVerification { startEmailVerification { exit() } }
        }else{
            exit()
        }
    }

    private fun fillInputs() {
        //Load profile image
        Picasso.get()
            .load(user.photoUrl)
            .placeholder(R.drawable.profile)
            .error(android.R.drawable.stat_notify_error)
            .into(binding.profileImage)

        binding.proNameInput.setText(user.displayName)
        binding.proEmailInput.setText(user.email)
        binding.proPhoneInput.setText(user.phoneNumber)
    }

    private fun changeProfilePicture() {
        //Open gallery
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 17)
    }

    private fun uploadProfileImage() {
        loading(true)
        val imageRef = storage.reference.child("users/${user.uid}.jpg")
        val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                .setDisplayName(binding.proNameInput.text.toString())

        //Changing profile image
            val baos = ByteArrayOutputStream()
            centerCropImage(profileUri!!).compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val data = baos.toByteArray()

            imageRef.putBytes(data)
                    .addOnSuccessListener {
                        imageRef.downloadUrl
                                .addOnSuccessListener {
                                    userProfileChangeRequest.photoUri = it
                                    saveUserProfile(userProfileChangeRequest.build())
                                }
                    }
    }


    private fun saveUserProfile(userProfileChangeRequest: UserProfileChangeRequest = UserProfileChangeRequest.Builder().setDisplayName(binding.proNameInput.text.toString()).build()) {
        user.updateProfile(userProfileChangeRequest)
                .addOnSuccessListener { findNavController().navigateUp().also { loading(false) } }
                .addOnFailureListener { Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG).show().also { loading(false) } }
    }
    
    private fun verifyInputs(): Boolean {
        binding.proNameLy.isErrorEnabled = true
        binding.proEmailLy.isErrorEnabled = true
        binding.proPhoneLy.isErrorEnabled = true

        if (binding.proNameInput.text.isNullOrBlank()) {
            binding.proNameLy.error = getString(R.string.empty_input,"Display name")
            return false
        } else if (binding.proEmailInput.text.isNullOrBlank()) {
            binding.proEmailLy.error = getString(R.string.empty_input,"Email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.proEmailInput.text.toString()).matches()) {
            binding.proEmailLy.error = getString(R.string.valid_input,"email")
            return false
        } else if (inputPhoneNumber.length > 5 && !Patterns.PHONE.matcher(inputPhoneNumber).matches()) {
            binding.proPhoneLy.error = getString(R.string.valid_input,"phone number")
            return false
        } else {
            binding.proEmailLy.apply {
                isErrorEnabled = false
                error = null
            }
            binding.proNameLy.apply {
                isErrorEnabled = false
                error = null
            }
            binding.proPhoneLy.apply {
                isErrorEnabled = false
                error = null
            }

            return true
        }
    }

    private fun verifyPasswordsInputs(): Boolean {
        binding.proPassLy.isErrorEnabled = true
        binding.proPassconLy.isErrorEnabled = true

        when {
            binding.proPassInput.text.toString().length > 8 -> {
                binding.proPassInput.error = getString(R.string.password_length)
                return false
            }
            binding.proPassconInput.text.isNullOrBlank() -> {
                binding.proPassconLy.error = getString(R.string.empty_input,"Password confirmation")
                return false
            }
            binding.proPassconInput.text.toString() != binding.proPassInput.text.toString() -> {
                binding.proPassconLy.error = getString(R.string.password_con_match)
                return false
            }
            else -> {
                binding.proPassLy.apply {
                    isErrorEnabled = false
                    error = null
                }
                binding.proPassconLy.apply {
                    isErrorEnabled = false
                    error = null
                }

                return true
            }
        }
    }

    private fun isPasswordChangeRequest() : Boolean{
        return !binding.proPassInput.text.isNullOrBlank()
    }

    private fun isPhoneChangeRequest(): Boolean {
        return inputPhoneNumber.length > 5 && inputPhoneNumber != user.phoneNumber
    }

    private fun startPhoneVerification(onSuccess : () -> Unit){
        val phoneVerificationFragment = PhoneVerificationFragment(inputPhoneNumber)
        phoneVerificationFragment.show(parentFragmentManager,"phone_verification")
        phoneVerificationFragment.verificationStatus.observe(viewLifecycleOwner) {
            when (it) {
                PhoneVerificationFragment.PhoneVerificationStatus.COMPLETED -> onSuccess().also { phoneVerificationFragment.dismiss() }
                PhoneVerificationFragment.PhoneVerificationStatus.ERROR -> {
                    binding.proPhoneLy.error = phoneVerificationFragment.verificationError
                    phoneVerificationFragment.dismiss()
                    loading(false)
                }
                PhoneVerificationFragment.PhoneVerificationStatus.CANCELED -> loading(false)
                else -> {}
            }
        }
    }

    private fun startEmailVerification(onSuccess: () -> Unit){
        val emailVerificationFragment = EmailChangeFragment(binding.proEmailInput.text.toString())
        emailVerificationFragment.show(parentFragmentManager,"email_verification")
        emailVerificationFragment.verificationStatus.observe(viewLifecycleOwner) {
            when (it) {
                EmailChangeFragment.EmailVerificationStatus.COMPLETED -> onSuccess().also { emailVerificationFragment.dismiss() }
                EmailChangeFragment.EmailVerificationStatus.ERROR -> {
                    binding.proPhoneLy.error = emailVerificationFragment.verificationError
                    emailVerificationFragment.dismiss()
                    loading(false)
                }
                EmailChangeFragment.EmailVerificationStatus.CANCELED -> loading(false)
                else -> {}
            }
        }
    }

    private fun isEmailChangeRequest() : Boolean{
        return binding.proEmailInput.text.toString() != user.email
    }

    private fun exit(){
        keyboardManager.value.closeKeyboard(requireView())

        if (profileUri != user.photoUrl){
            uploadProfileImage()
        }else{
            saveUserProfile()
        }
    }

    private fun centerCropImage(imageUri: Uri): Bitmap {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, imageUri))
        } else {
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
        }

        val dimension = bitmap.width.coerceAtMost(bitmap.height)
        return ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 17 && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data!!
            profileUri = imageUri
            val bitmap = centerCropImage(imageUri)

            binding.profileImage.setImageBitmap(bitmap)
        }
    }
}
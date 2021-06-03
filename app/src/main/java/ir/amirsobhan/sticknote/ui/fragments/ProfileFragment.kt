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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.FragmentProfileBinding
import ir.amirsobhan.sticknote.ui.fragments.auth.PhoneVerificationFragment
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    val auth = Firebase.auth
    val user = auth.currentUser!!
    var profileUri = user.photoUrl
    var uploadedUri: Uri? = null
    val storage = Firebase.storage

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)

        setupToolbar()
        fillInputs()

        binding.profileImage.setOnClickListener { changeProfilePicture() }

        return binding.root
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.profile_menu_save -> if (verifyInputs()) {startRouting()}
            }
            true
        }
    }

    private fun loading(bool: Boolean) {
        if (bool) {
            binding.toolbar.menu[0].actionView = ProgressBar(requireContext())
        } else {
            binding.toolbar.menu[0].actionView = null
        }
    }

    private fun startRouting(){
        if (isPasswordChangeRequest()){
            if (verifyPasswordsInputs()){
                user.updatePassword(binding.proPassInput.text.toString())
            }
        }

        if (isPhoneChangeRequest() && !isEmailChangeRequest()){
            val phoneVerificationFragment = PhoneVerificationFragment(binding.proPhoneInput.text.toString())
            phoneVerificationFragment.show(parentFragmentManager,"Phone")

            phoneVerificationFragment.isPhoneVerified.observe(viewLifecycleOwner, Observer {
                  if (it){
                      exit()
                  }
            })

        }else if(!isPhoneChangeRequest() && isEmailChangeRequest()){

        }else if (isPhoneChangeRequest() && isEmailChangeRequest())

        if (profileUri != user.photoUrl){
            uploadProfileImage()
        }else{
            saveUserProfile()
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

        if (profileUri != user.photoUrl) {
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
        } else {
            saveUserProfile(userProfileChangeRequest.build())
        }
    }

    private fun saveUserProfile(userProfileChangeRequest: UserProfileChangeRequest = UserProfileChangeRequest.Builder().setDisplayName(binding.proNameInput.text.toString()).build()) {
        user.updateProfile(userProfileChangeRequest)
                .addOnSuccessListener { exit() }
                .addOnFailureListener { Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG).show().also { loading(false) } }
    }

    private fun verifyPhoneNumber(callback : PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        val phoneAuthOptions = PhoneAuthOptions.Builder(auth)
                .setPhoneNumber(binding.proPhoneInput.text.toString())
                .setTimeout(120L, TimeUnit.SECONDS)
                .setCallbacks(callback)
                .setActivity(requireActivity())
                .build()
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
    }

    private fun verifyInputs(): Boolean {
        binding.proNameLy.isErrorEnabled = true
        binding.proEmailLy.isErrorEnabled = true
        binding.proPhoneLy.isErrorEnabled = true

        if (binding.proNameInput.text.isNullOrBlank()) {
            binding.proNameLy.error = "Display name cannot be empty"
            return false
        } else if (binding.proEmailInput.text.isNullOrBlank()) {
            binding.proEmailLy.error = "Email cannot be empty"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.proEmailInput.text.toString()).matches()) {
            binding.proEmailLy.error = "Enter a valid email"
            return false
        } else if (!binding.proPhoneInput.text.isNullOrBlank() && !Patterns.PHONE.matcher(binding.proPhoneInput.text.toString()).matches()) {
            binding.proPhoneLy.error = "Enter a valid phone number"
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
                binding.proPassInput.error = "Password length should 8 or above"
                return false
            }
            binding.proPassconInput.text.isNullOrBlank() -> {
                binding.proPassconLy.error = "Password confirmation cannot be empty"
                return false
            }
            binding.proPassconInput.text.toString() != binding.proPassInput.text.toString() -> {
                binding.proPassconLy.error = "Password confirmation doesn't match password"
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

    private fun isPhoneChangeRequest() : Boolean{
        return !binding.proPhoneInput.text.isNullOrBlank()
    }

    private fun isEmailChangeRequest() : Boolean{
        return binding.proEmailInput.text.toString() != user.email
    }

    private fun exit(){
        findNavController().navigateUp().also { loading(false) }
    }

    private fun centerCropImage(imageUri: Uri): Bitmap {
        var bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, imageUri))
        } else {
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
        }

        if (bitmap == null) {

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
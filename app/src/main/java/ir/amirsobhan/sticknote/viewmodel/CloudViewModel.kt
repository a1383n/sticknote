package ir.amirsobhan.sticknote.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.repositories.NoteRepository
import org.koin.java.KoinJavaComponent.inject

class CloudViewModel() : ViewModel() {

    companion object{
        const val ACTIVITY_RESULT_REQUEST_CODE = 14
    }

    private val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build(), AuthUI.IdpConfig.EmailBuilder().build())
    val firebaseAuth : FirebaseAuth by inject(FirebaseAuth::class.java)

    val reps : NoteRepository by inject(NoteRepository::class.java)

    val user: FirebaseUser get() = firebaseAuth.currentUser

    fun createAuthUIIntent(): Intent {
        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.logo)
            .build()
    }

    fun isUserSignIn() : Boolean = firebaseAuth.currentUser != null

}
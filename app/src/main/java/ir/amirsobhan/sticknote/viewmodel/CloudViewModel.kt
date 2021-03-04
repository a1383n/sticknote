package ir.amirsobhan.sticknote.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import org.koin.java.KoinJavaComponent.inject

class CloudViewModel(val app: Application) : AndroidViewModel(app) {
    val gsc : GoogleSignInClient by inject(GoogleSignInClient::class.java)
    val GOOGLE_SIGN_IN_RC = 14


    fun isLogin() : Boolean = GoogleSignIn.getLastSignedInAccount(app) != null


    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) : Boolean {
        return try {
            completedTask.getResult(ApiException::class.java)
            true
        } catch (e: ApiException) {
            false
        }
    }
}
package ir.amirsobhan.sticknote.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.ui.activity.AuthActivity
import ir.amirsobhan.sticknote.worker.AutoSync
import org.koin.android.ext.android.inject

class SettingFragment : PreferenceFragmentCompat(){

    var auth = Firebase.auth
    private val workManager : WorkManager by inject()
    private val work = AutoSync.Factory(AutoSync.SYNC)
    private val isUserSignIn get() = auth.currentUser != null
    private var user : FirebaseUser? = auth.currentUser
    private lateinit var themeListPreference: ListPreference
    private lateinit var accountInfo: Preference
    private lateinit var accountEdit: Preference
    private lateinit var accountVerify: Preference
    private lateinit var accountLogout: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_setting, rootKey)
        themeListPreference = findPref(getString(R.string.setting_general_theme))
        accountInfo = findPref(getString(R.string.setting_acc_info))
        accountEdit = findPref(getString(R.string.setting_acc_edit))
        accountVerify = findPref(getString(R.string.setting_acc_verify))
        accountLogout = findPref(getString(R.string.setting_acc_logout))

        fillPreferences(isUserSignIn)

        if (isUserSignIn && !user?.isEmailVerified!!) {
            showEmailVerifyPreference()
        }
        setPreferencesListener()

        themeListPreference.setOnPreferenceChangeListener { _, newValue ->
            AppCompatDelegate.setDefaultNightMode(newValue.toString().toInt())
            true
        }
    }

    private fun <T : Preference?> findPref(key: CharSequence): T {
        if (preferenceManager != null){
            val preference =  preferenceManager.findPreference<T>(key)
            if (preference != null){
                return preference
            }else{
                throw Resources.NotFoundException("View with $key key not found")
            }
        }else{
            throw NullPointerException("preferenceManger is null")
        }
    }

    private fun fillPreferences(isUserSignIn : Boolean){
        if (isUserSignIn) {
            val user = auth.currentUser!!
            accountInfo.title = user.displayName
            accountInfo.summary = getString(R.string.setting_login_as, user.email)
            accountInfo.isSelectable = false
        }else{
            accountInfo.title = getString(R.string.setting_not_login)
            accountInfo.summary = getString(R.string.setting_click_login)
            accountInfo.setOnPreferenceClickListener {
                activityResultLauncher.launch(Intent(requireContext(),AuthActivity::class.java))
                true
            }

            accountEdit.isVisible = false
            accountLogout.isVisible = false
        }
    }

    private fun showEmailVerifyPreference(){
        val successMessage = Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.setting_verification_send, Snackbar.LENGTH_LONG)
        accountVerify.isVisible = true
        accountVerify.setOnPreferenceClickListener {
            user?.sendEmailVerification()?.addOnSuccessListener {
                successMessage.show()
                accountVerify.isVisible = false
            }?.addOnFailureListener { Toast.makeText(context, it.message, Toast.LENGTH_LONG).show() }
            true
        }
    }

    private fun logoutUser(){
        val progressDialog = ProgressDialog(requireContext()).apply { setMessage("Loading...") }
        progressDialog.show()
        val gso : GoogleSignInOptions by inject()
        val gsc = GoogleSignIn.getClient(requireContext(),gso)

        workManager.enqueue(work)
        workManager.getWorkInfoByIdLiveData(work.id).observe(viewLifecycleOwner, {
            if (it.state == WorkInfo.State.SUCCEEDED){
                auth.signOut()
                gsc.signOut()
                progressDialog.hide()
                reload()
            }else if (it.state == WorkInfo.State.FAILED){
                progressDialog.hide()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.warning)
                    .setMessage(R.string.setting_last_sync_error)
                    .setPositiveButton(R.string.yes) {_, _ ->
                        auth.signOut()
                        gsc.signOut()
                        reload()
                    }
                    .setNegativeButton(R.string.no) {_, _ ->}
                    .create().show()
            }
        })
    }

    private fun setPreferencesListener(){

        // Logout preference
        accountLogout.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.setting_logout_msg)
                .setPositiveButton(R.string.ok) { _, _ -> logoutUser() }
                .setNegativeButton(R.string.cancel) {_ , _ ->}
                .setIcon(R.drawable.logout)
                .create().show()
            true
        }

        // Edit preference
        accountEdit.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_profileFragment)
            true
        }
    }

    private fun reload() {
        findNavController().navigate(R.id.action_settingFragment_self)
    }

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK){
            reload()
            workManager.enqueue(work)
            Firebase.auth.currentUser?.uid?.let { uid ->
                Firebase.crashlytics.setUserId(uid)
                Firebase.analytics.setUserId(uid)
            }
        }
    }
}

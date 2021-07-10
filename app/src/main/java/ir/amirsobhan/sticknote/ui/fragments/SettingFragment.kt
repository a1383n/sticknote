package ir.amirsobhan.sticknote.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.ui.activity.AuthActivity
import ir.amirsobhan.sticknote.worker.AutoSync
import org.koin.android.ext.android.inject

class SettingFragment : PreferenceFragmentCompat(){
    var auth = Firebase.auth

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_setting, rootKey)
        var themeListPreference: ListPreference? = findPreference("theme")
        var accountInfo: Preference = findPreference("account_info")!!
        var accountEdit: Preference = findPreference("account_edit")!!
        var accountVerify: Preference = findPreference("account_verify")!!
        var accountLogout: Preference = findPreference("account_logout")!!

        if (auth.currentUser != null) {
            val user = auth.currentUser!!
            accountInfo.title = user.displayName
            accountInfo.summary = "Your login as ${user.email}"
            accountInfo.isSelectable = false

            if (!user.isEmailVerified) {
                val successMessage = Snackbar.make(requireActivity().findViewById(android.R.id.content), "Verification link send to your email,Check your inbox", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.floatingAction)
                accountVerify.isVisible = true
                accountVerify.setOnPreferenceClickListener {
                    user?.sendEmailVerification().addOnSuccessListener {
                        successMessage.show()
                        accountVerify.isVisible = false
                    }.addOnFailureListener { Toast.makeText(context, it.message, Toast.LENGTH_LONG).show() }
                    true
                }
            }

            accountLogout.setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Logout?")
                        .setMessage("Are you sure you want to logout?\nCloud saving will stop after that")
                        .setPositiveButton("OK,Logout") { _, _ -> logoutUser() }
                        .setNegativeButton("Cancel") {_ , _ ->}
                        .setIcon(R.drawable.logout)
                        .create().show()
                true
            }
            accountEdit.setOnPreferenceClickListener {
                findNavController().navigate(R.id.action_settingFragment_to_profileFragment)
                true
            }
        } else {
            accountInfo.title = "You're not login"
            accountInfo.summary = "Click to login"
            accountInfo.setOnPreferenceClickListener { startActivityForResult(Intent(activity, AuthActivity::class.java),10).run { true } }

            accountEdit.isVisible = false
            accountLogout.isVisible = false
        }


        themeListPreference?.setOnPreferenceChangeListener { preference, newValue -> AppCompatDelegate.setDefaultNightMode(newValue.toString().toInt()).run { true } }
    }

    private fun logoutUser(){
        val progressDialog = ProgressDialog(requireContext()).apply { setMessage("Loading...") }
        progressDialog.show()
        val workManager : WorkManager by inject()
        val work = AutoSync.Factory(AutoSync.SYNC)
        val gso : GoogleSignInOptions by inject()
        val gsc = GoogleSignIn.getClient(requireContext(),gso)

        workManager.enqueue(work)
        workManager.getWorkInfoByIdLiveData(work.id).observe(viewLifecycleOwner, Observer {
            if (it.state == WorkInfo.State.SUCCEEDED){
                auth.signOut()
                gsc.signOut()
                progressDialog.hide()
                reload()
            }else if (it.state == WorkInfo.State.FAILED){
                progressDialog.hide()
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Warning")
                        .setMessage("We couldn't do the last cloud sync,Are you sure you want to log out without sync?")
                        .setPositiveButton("Yes,Force logout") {_, _ ->
                            auth.signOut()
                            gsc.signOut()
                            reload()
                        }
                        .setNegativeButton("No") {_, _ ->}
                        .create().show()
            }
        })
    }


    private fun reload() {
        findNavController().navigate(R.id.action_settingFragment_self)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode == Activity.RESULT_OK){
            reload()
        }
    }

}

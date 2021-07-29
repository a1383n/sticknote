package ir.amirsobhan.sticknote.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.storage.ktx.storage
import ir.amirsobhan.sticknote.BuildConfig
import ir.amirsobhan.sticknote.Constants
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.SplashActivityBinding
import ir.amirsobhan.sticknote.helper.DownloadController


class SplashActivity : AppCompatActivity() {

    companion object {
        private const val STORAGE_REQ_CODE = 102
    }

    private lateinit var splashActivityBinding: SplashActivityBinding
    private lateinit var handler: Handler
    private val remoteConfig = Firebase.remoteConfig
    private val storage = Firebase.storage
    private var permissionDenied = false

    @SuppressLint("ResourceType")
    @SuppressWarnings()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashActivityBinding = SplashActivityBinding.inflate(layoutInflater)
        setContentView(splashActivityBinding.root)

        //Colorful app name text
        val string = getString(R.string.splash_activity_html_text,"#0D5D44")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            splashActivityBinding.appName.text = Html.fromHtml(string,Html.FROM_HTML_MODE_COMPACT)
        }else{
            splashActivityBinding.appName.text = Html.fromHtml(string)
        }


        //Check for update
        if (remoteConfig[Constants.RemoteConfig.APP_VERSION].asDouble() < BuildConfig.VERSION_NAME.toDouble()) {
            MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
                .setTitle(R.string.splash_activity_update_available_title)
                .setMessage(R.string.splash_activity_update_available_message)
                .setIcon(R.drawable.ic_baseline_update_24)
                .setPositiveButton(R.string.update_now) { _, _ -> updateApplication() }
                .setNegativeButton(R.string.later) { _, _ ->
                    startMainActivity(500)
                    /**LOL**/
                }
                .setOnCancelListener { startMainActivity(500) }
                .show()
        } else {
            //Start MainActivity after 1.5 second
            startMainActivity(1500)
        }

    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_REQ_CODE
        )
    }

    private fun startMainActivity(delayMillis: Long) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        handler = Handler(mainLooper)
        handler.postDelayed({
            finish()
            startActivity(intent)
        }, delayMillis)
    }

    private fun updateApplication() {
        storage.reference.child("app-release.apk").downloadUrl
            .addOnSuccessListener {
                if (checkPermission()) {
                    downloadApk(it)
                } else {
                    requestPermission()
                }
            }
    }

    private fun downloadApk(uri: Uri) {
        DownloadController(this, uri)
            .enqueueDownload()
            .downloadStatus.observe(this, {
                splashActivityBinding.updateError.apply {
                    isVisible = true
                    text = it.name
                }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_REQ_CODE) {
            permissions.mapIndexed { index, _ ->
                if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                    updateApplication()
                } else {
                    if (!permissionDenied) {
                        MaterialAlertDialogBuilder(this)
                            .setTitle(R.string.permission_denied)
                            .setMessage(R.string.permission_denied_message)
                            .setPositiveButton(R.string.im_sure) { _, _ -> startMainActivity(300) }
                            .setNegativeButton(R.string.retry) { _, _ -> requestPermission().also { permissionDenied = true } }
                            .setCancelable(false)
                            .show()
                    }else{
                        MaterialAlertDialogBuilder(this)
                            .setMessage(R.string.permission_dialog_block)
                            .setPositiveButton(R.string.ok){_ , _ -> startMainActivity(150)}
                            .setCancelable(false)
                            .show()
                    }
                }
            }
        }
    }
}
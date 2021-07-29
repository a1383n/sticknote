package ir.amirsobhan.sticknote.helper

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import ir.amirsobhan.sticknote.BuildConfig
import ir.amirsobhan.sticknote.Constants
import ir.amirsobhan.sticknote.R
import java.io.File

class DownloadController(private val context: Context,private val url: Uri) {

    companion object{
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val PROVIDER_PATH = ".provider"
        private const val APP_INSTALL_PATH = "\"application/vnd.android.package-archive\""
    }

    private val fileName = Constants.DOWNLOAD_CONTROLLER_APP_NAME
    val downloadStatus = MutableLiveData(DownloadStatus.IDLE)

    fun enqueueDownload(): DownloadController {
        // Get download folder Uri & plus it to file name
        var destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += fileName

        // Add file base path to Uri
        val uri = Uri.parse("$FILE_BASE_PATH$destination")

        // Check file is exists if it exist delete that file
        val file = File(destination)
        if (file.exists()) file.delete()

        //Get DownloadManager system service
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // Make download request
        val request = DownloadManager.Request(url)
            .setMimeType(MIME_TYPE)
            .setTitle(context.getString(R.string.download_controller_notification_title))
            .setDescription(context.getString(R.string.download_controller_notification_des))
            .setDestinationUri(uri)

        //Enqueue download request
        downloadManager.enqueue(request).also { downloadStatus.postValue(DownloadStatus.DOWNLOADING) }
        showInstallOption(destination,uri)

        return this
    }

    /**
     * Show APK install dialog
     * @param destination The file in download folder Uri
     * @param uri The file was downloaded by download manager Uri
     */
    private fun showInstallOption(destination : String, uri: Uri){
        // set BroadcastReceiver to install app when .apk is downloaded
        val onComplete = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                downloadStatus.postValue(DownloadStatus.INSTALLING)

                // If android version 7 or above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    val contentUri = FileProvider.getUriForFile(context!!,BuildConfig.APPLICATION_ID+ PROVIDER_PATH,File(destination))
                    val install = Intent(Intent.ACTION_VIEW)
                        .apply {
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE,true)
                            data = contentUri
                        }
                    context.startActivity(install)
                    context.unregisterReceiver(this)
                }else{
                    //Android version 7 or lower
                    val install = Intent(Intent.ACTION_VIEW)
                        .apply {
                            setDataAndType(uri, APP_INSTALL_PATH)
                        }

                    // Show install dialog and unregisterReceiver
                    context?.startActivity(install)
                    context?.unregisterReceiver(this)
                }
            }
        }

        // Register receiver to trick when download complete
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    /**
     * The enum class for different download status
     */
    enum class DownloadStatus{
        IDLE,
        DOWNLOADING,
        INSTALLING
    }
}
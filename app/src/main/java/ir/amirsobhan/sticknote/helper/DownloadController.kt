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
import java.io.File

class DownloadController(private val context: Context,private val url: Uri) {
    companion object{
        private const val FILE_NAME = "stickMeNote.apk"
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val PROVIDER_PATH = ".provider"
        private const val APP_INSTALL_PATH = "\"application/vnd.android.package-archive\""
    }

    val downloadStatus = MutableLiveData(DownloadStatus.IDLE)

    fun enqueueDownload(): DownloadController {
        var destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += FILE_NAME

        val uri = Uri.parse("$FILE_BASE_PATH$destination")

        val file = File(destination)
        if (file.exists()) file.delete()

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(url)
            .setMimeType(MIME_TYPE)
            .setTitle("Downloading...")
            .setDescription("New version of app is downloading")
            .setDestinationUri(uri)

        downloadManager.enqueue(request).also { downloadStatus.postValue(DownloadStatus.DOWNLOADING) }
        showInstallOption(destination,uri)

        return this
    }

    private fun showInstallOption(destination : String, uri: Uri){
        // set BroadcastReceiver to install app when .apk is downloaded
        val onComplete = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                downloadStatus.postValue(DownloadStatus.INSTALLING)
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
                    val install = Intent(Intent.ACTION_VIEW)
                        .apply {
                            setDataAndType(uri, APP_INSTALL_PATH)
                        }
                    context?.startActivity(install)
                    context?.unregisterReceiver(this)
                }
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    enum class DownloadStatus{
        IDLE,
        DOWNLOADING,
        INSTALLING
    }
}
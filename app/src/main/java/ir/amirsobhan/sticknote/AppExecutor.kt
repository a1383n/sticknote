package ir.amirsobhan.sticknote

import android.app.Application
import android.content.Context
import androidx.core.content.ContextCompat
import ir.amirsobhan.sticknote.database.AppDatabase
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AppExecutor {

    fun diskIO(): ExecutorService {
        return Executors.newSingleThreadExecutor()
    }

    fun mainThread(context: Context): Executor {
        return ContextCompat.getMainExecutor(context)
    }
}
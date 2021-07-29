package ir.amirsobhan.sticknote

import android.content.Context
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

fun diskIO(): ExecutorService {
    return Executors.newSingleThreadExecutor()
}

fun mainThread(context: Context): Executor {
    return ContextCompat.getMainExecutor(context)
}


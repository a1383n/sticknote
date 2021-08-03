package ir.amirsobhan.sticknote

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.testing.TestWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.worker.AutoSync
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class AutoSyncWorkerTest {
    private lateinit var context: Context
    private lateinit var executor: Executor
    private val isUserSignInt get() = Firebase.auth.currentUser != null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        executor = Executors.newSingleThreadExecutor()

    }

    @Test
    fun testWorker() {
        val worker = TestWorkerBuilder<AutoSync>(context,executor, Data.Builder().putAll(mapOf("action" to "sync")).build())
            .build()
        val result = worker.doWork()

        if (isUserSignInt) {
            assertThat(result).isEqualTo(ListenableWorker.Result.success())
        }else{
            assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        }
    }
}
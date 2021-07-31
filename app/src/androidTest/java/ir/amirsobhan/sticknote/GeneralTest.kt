package ir.amirsobhan.sticknote

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeneralTest{
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun testPackageName(){
        assertThat("ir.amirsobhan.sticknote").isEqualTo(context.packageName)
    }
}
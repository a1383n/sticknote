package ir.amirsobhan.sticknote.ui.activity

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.database.AppDatabase
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.utils.fillEditor
import ir.amirsobhan.sticknote.utils.withHtml
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@LargeTest
class NoteActivityTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val database = Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
    private val testTitle = "My test title"
    private val testBody = "<b>My test note content</b>"
    private val note = Note(id = "test",title = testTitle,text = testBody)

    @get:Rule
    var activityRule: ActivityScenarioRule<NoteActivity>
            = ActivityScenarioRule(NoteActivity::class.java)

    @Test
    fun createEmptyNoteTest(){
        // Check is our field is empty
        onView(withId(R.id.toolbarEditText))
            .check(matches(withHint(R.string.note_activity_untitled)))
            .check(matches(withText("")))
            .perform(typeText(testTitle))

        onView(withId(R.id.body))
            .check(withHtml(""))
            .perform(fillEditor(testBody))

        CountDownLatch(1).await(1,TimeUnit.SECONDS)

        onView(withId(R.id.save))
            .perform(click())

        CountDownLatch(1).await(1,TimeUnit.SECONDS)

        database.noteDao().exportAll().find {
            Truth.assertThat(it.text == testTitle && it.text == testBody).isTrue()

            false
        }
    }
}
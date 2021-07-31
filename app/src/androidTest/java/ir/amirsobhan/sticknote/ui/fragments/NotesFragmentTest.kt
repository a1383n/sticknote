package ir.amirsobhan.sticknote.ui.fragments

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.common.truth.Truth.assertThat
import ir.amirsobhan.sticknote.Constants
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.database.AppDatabase
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.utils.RecyclerViewItemCountAssertion
import ir.amirsobhan.sticknote.utils.RecyclerViewSimpleItemCheckAssertion
import ir.amirsobhan.sticknote.viewmodel.NoteViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotesFragmentTest {
    private lateinit var scenario: FragmentScenario<NotesFragment>
    private lateinit var database: AppDatabase
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var viewModel : NoteViewModel

    @Before
    fun setUp(){
        database = Room.databaseBuilder(context,AppDatabase::class.java,Constants.APP_DATABASE_NAME)
            .build()

        database.noteDao().deleteAll()
        database.noteDao().insert(Note(title = "TEST",text = "BODY TEST"))

        scenario = launchFragmentInContainer(
            null,
            R.style.Theme_StickNote,
            Lifecycle.State.STARTED
        )

        scenario.onFragment{
            viewModel = it.viewModel
        }
    }

    @Test
    fun launchFragmentTest(){
        scenario.onFragment{
            assertThat(it.activity).isNotNull()
            assertThat(it.view).isNotNull()
            assertThat(it.context).isNotNull()
        }
    }

    @Test
    fun recyclerViewItemCountTest(){
        onView(withId(R.id.recyclerView))
            .check(RecyclerViewItemCountAssertion(database.noteDao().exportAll().size))
    }

    @Test
    fun recyclerViewItemsEqualToDatabase(){
        onView(withId(R.id.recyclerView))
            .check(RecyclerViewSimpleItemCheckAssertion(database.noteDao().exportAll()))
    }

    @Test
    fun actionModeTest(){
        // Long click on item in recyclerView
        onView(withId(R.id.materialCardView))
            .perform(longClick())

        // Check actionMode is activated or not
        onView(withId(R.id.toolbar)).check { view, noViewFoundException ->
            if (noViewFoundException != null) {
                throw noViewFoundException
            }
            view as MaterialToolbar
            assertThat(viewModel.actionMode != null).isTrue()
            assertThat(viewModel.selectedNote.value?.size).isGreaterThan(0)
        }

        // Click on the same item
        onView(withId(R.id.materialCardView))
            .perform(ViewActions.click())


        // Checked toolbar, actionMode should be null and not be active
        onView(withId(R.id.toolbar)).check { view, noViewFoundException ->
            if (noViewFoundException != null) {
                throw noViewFoundException
            }

            view as MaterialToolbar
            assertThat(viewModel.actionMode == null).isTrue()
            assertThat(viewModel.selectedNote.value?.size).isEqualTo(0)
        }

        // Checked item, should be [isChecked] equals to false
        onView(withId(R.id.materialCardView)).check { view, noViewFoundException ->
            if (noViewFoundException != null) {
                throw noViewFoundException
            }

            view as MaterialCardView
            assertThat(view.isChecked).isFalse()
        }
    }

    @After
    fun closeDB(){
        database.noteDao().deleteAll()
        database.close()
    }
}
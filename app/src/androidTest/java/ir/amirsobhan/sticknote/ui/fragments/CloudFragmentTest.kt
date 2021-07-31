package ir.amirsobhan.sticknote.ui.fragments

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import ir.amirsobhan.sticknote.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CloudFragmentTest {
    private lateinit var scenario : FragmentScenario<CloudFragment>
    private var isUserSignIn : Boolean = false

    @Before
    fun setUp() {
        scenario = launchFragmentInContainer(null,R.style.Theme_StickNote,Lifecycle.State.STARTED)
        scenario.onFragment {
            isUserSignIn = it.isUserSignedIn
        }
    }

    @Test
    fun isUserSignInTest(){
        if (isUserSignIn) {
            onView(withId(R.id.cloudCard))
                .check(matches(isDisplayed()))
        }else{
            onView(withId(R.id.signInCard))
                .check(matches(isDisplayed()))
        }
    }
}
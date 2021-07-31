package ir.amirsobhan.sticknote.utils

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.google.common.truth.Truth.assertThat
import jp.wasabeef.richeditor.RichEditor
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher


fun fillEditor(htmlText : String): ViewAction {
    return object : ViewAction{
        override fun getConstraints(): Matcher<View> {
            return allOf(isDisplayed(), isAssignableFrom(RichEditor::class.java))
        }

        override fun getDescription(): String = "fill editor"

        override fun perform(uiController: UiController?, view: View?) {
            view as RichEditor
            view.html = htmlText
        }
    }
}

fun withHtml(htmlText: String): ViewAssertion {
    return ViewAssertion { v, e ->
        if (e != null) {
            throw e
        }

        v as RichEditor
        assertThat(v.html).isEqualTo(htmlText)
    }
}
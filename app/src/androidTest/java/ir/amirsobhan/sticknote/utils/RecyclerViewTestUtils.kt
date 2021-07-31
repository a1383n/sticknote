package ir.amirsobhan.sticknote.utils

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import com.google.common.truth.Truth.assertThat
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.database.Note


class RecyclerViewItemCountAssertion(private val expectedCount: Int) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) {
            throw noViewFoundException
        }
        val recyclerView = view as RecyclerView
        val adapter = recyclerView.adapter
        assertThat(adapter?.itemCount).isEqualTo(expectedCount)
    }
}

class RecyclerViewSimpleItemCheckAssertion(private val list: List<Note>) : ViewAssertion{
    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) {
            throw noViewFoundException
        }


        val recyclerView = view as RecyclerView
        var i = 0
        while (recyclerView.childCount > i){
            val note = list[i]
            val view = recyclerView.getChildAt(i)
            assertThat(view.findViewById<TextView>(R.id.title).text).isEqualTo(note.title)
            i++
        }
    }

}
package ir.amirsobhan.sticknote.helper

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager

class KeyboardManager(val context: Context) {

    var inputMethodManager: InputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    fun showKeyboard(view: View) {
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun closeKeyboard(view: View) {
        inputMethodManager.hideSoftInputFromWindow(view.windowToken,InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    fun toggleKeyboard(){
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }
}
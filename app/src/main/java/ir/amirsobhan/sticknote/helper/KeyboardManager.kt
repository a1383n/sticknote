package ir.amirsobhan.sticknote.helper

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

class KeyboardManager(val context: Context) {

    var inputMethodManager: InputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    /**
     * Close soft keyboard
     * @param view The view keyboard focused on that
     */
    fun closeKeyboard(view: View) {
        inputMethodManager.hideSoftInputFromWindow(view.windowToken,0)
    }

}
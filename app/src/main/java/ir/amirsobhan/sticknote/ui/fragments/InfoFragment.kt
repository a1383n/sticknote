package ir.amirsobhan.sticknote.ui.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ir.amirsobhan.sticknote.BuildConfig
import ir.amirsobhan.sticknote.R
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

class InfoFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root : View =
            AboutPage(context)
                .enableDarkMode(isNightMode())
                .setImage(R.drawable.logo)
                .setDescription("${getString(R.string.app_name)} is a simple notepad.\n" + "This app is a easy and fast notes app.")
                .addGitHub("https://github.com/a1383n/sticknote")
                .addInstagram("amirsobhan1553","Follow me on Instagram")
                .addEmail("amirsobhan1553@gmail.com")
                .addGroup("")
                .addItem(Element("Version: ${BuildConfig.VERSION_NAME} ${BuildConfig.BUILD_TYPE}",null))
                .create()


        if (isNightMode()) {
            val linearLayout: LinearLayout = root.findViewById(R.id.sub_wrapper)
            linearLayout.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.window_bg))
        }


            return root
    }

    fun isNightMode() : Boolean = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}
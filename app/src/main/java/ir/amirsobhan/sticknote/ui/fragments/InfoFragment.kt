package ir.amirsobhan.sticknote.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ir.amirsobhan.sticknote.BuildConfig
import ir.amirsobhan.sticknote.Constants
import ir.amirsobhan.sticknote.R
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

class InfoFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root : View =
            AboutPage(context)
                .enableDarkMode(Constants.isDarkMode(requireContext()))
                .setImage(R.drawable.logo)
                .setDescription(getString(R.string.app_description,getString(R.string.app_name)))
                .addGitHub("a1383n/sticknote")
                .addInstagram("amirsobhan1553","Follow me on Instagram")
                .addEmail("amirsobhan1553@gmail.com")
                .addGroup("")
                .addItem(Element(getString(R.string.app_version,BuildConfig.VERSION_NAME,BuildConfig.BUILD_TYPE),null))
                .create()


        if (Constants.isDarkMode(requireContext())) {
            val linearLayout: LinearLayout = root.findViewById(R.id.sub_wrapper)
            linearLayout.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.window_bg))
        }


            return root
    }
}
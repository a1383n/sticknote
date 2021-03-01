package ir.amirsobhan.sticknote.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import ir.amirsobhan.sticknote.ui.fragments.CloudFragment
import ir.amirsobhan.sticknote.ui.fragments.InfoFragment
import ir.amirsobhan.sticknote.ui.fragments.NotesFragment
import ir.amirsobhan.sticknote.ui.fragments.SettingFragment

class MainViewPagerAdapter(val fragmentManager: FragmentManager,val lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 4;
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SettingFragment()
            1 -> CloudFragment()
            2 -> NotesFragment()
            3 -> InfoFragment()

            else -> TODO();
        }
    }
}
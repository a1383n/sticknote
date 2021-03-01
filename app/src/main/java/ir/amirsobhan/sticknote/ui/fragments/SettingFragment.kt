package ir.amirsobhan.sticknote.ui.fragments

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ir.amirsobhan.sticknote.R

class SettingFragment : PreferenceFragmentCompat(){
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_setting,rootKey)
        var themeListPreference : ListPreference? = findPreference("theme")

        themeListPreference?.setOnPreferenceChangeListener { preference, newValue -> AppCompatDelegate.setDefaultNightMode(newValue.toString().toInt()).run { true } }
    }

}
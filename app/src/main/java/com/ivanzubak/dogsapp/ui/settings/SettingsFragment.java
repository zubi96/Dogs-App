package com.ivanzubak.dogsapp.ui.settings;

import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.widget.Toast;

import com.ivanzubak.dogsapp.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    public SettingsFragment() { }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
        EditTextPreference cacheDurationPref = (EditTextPreference) findPreference("pref_cache_duration");
        cacheDurationPref.setOnPreferenceChangeListener(numberCheckListener);
    }

    private Preference.OnPreferenceChangeListener numberCheckListener =
            (preference, newValue) -> isNumber(newValue.toString());

    private boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getContext(), "Invalid input, no changes saved", Toast.LENGTH_LONG).show();
            return false;
        }
    }
}

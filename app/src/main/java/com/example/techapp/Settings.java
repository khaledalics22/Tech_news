package com.example.techapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public static class TechReportPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_list_order_by_key)));
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String value = newValue.toString();
            preference.setSummary(value);
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(value);
                if (prefIndex >= 0) {
                    CharSequence[] labels = listPreference.getEntries();
                    listPreference.setSummary(labels[prefIndex]);
                }
            } else {
                preference.setSummary(value);
            }
            return true;
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences prefs = getPreferenceManager().getDefaultSharedPreferences(preference.getContext());
            String preferenceString = prefs.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }

    }
}

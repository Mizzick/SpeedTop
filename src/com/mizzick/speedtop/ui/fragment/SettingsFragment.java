package com.mizzick.speedtop.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import com.mizzick.speedtop.R;

/**
 * Preferences fragment
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
			Preference preference = getPreferenceScreen().getPreference(i);
			if (preference instanceof PreferenceGroup) {
				PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
				for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
					updatePreference(preferenceGroup.getPreference(j));
				}
			} else {
				updatePreference(preference);
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updatePreference(findPreference(key));
	}

	private void updatePreference(Preference preference) {
		if (preference instanceof EditTextPreference) {
			EditTextPreference editTextPref = (EditTextPreference) preference;
			preference.setSummary(editTextPref.getText());
		} else if (preference instanceof ListPreference) {
			ListPreference listPreference = (ListPreference) preference;
			listPreference.setSummary(listPreference.getEntry());
		}
	}
}

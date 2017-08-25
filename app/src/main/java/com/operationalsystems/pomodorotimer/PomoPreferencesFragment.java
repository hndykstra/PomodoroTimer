package com.operationalsystems.pomodorotimer;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Main Pomodoro preferences fragment, loaded from preferences.xml
 */
public class PomoPreferencesFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    String breakTimePrefKey;
    String activityTimePrefKey;
    String breakTimeSummary;
    String activityTimeSummary;

    public PomoPreferencesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        breakTimePrefKey = getActivity().getString(R.string.pref_break_time_key);
        breakTimeSummary = getActivity().getString(R.string.pref_break_time_smry);
        activityTimePrefKey = getActivity().getString(R.string.pref_activity_time_key);
        activityTimeSummary = getActivity().getString(R.string.pref_activity_time_smry);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        updatePreferenceSummary(findPreference(breakTimePrefKey), breakTimeSummary);
        updatePreferenceSummary(findPreference(activityTimePrefKey), activityTimeSummary);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity() != null) {
            SharedPreferences preferences = getPreferenceManager().getDefaultSharedPreferences(getActivity());
            preferences.registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (breakTimePrefKey.equals(key)) {
            updatePreferenceSummary(findPreference(key), breakTimeSummary);
        } else if (activityTimePrefKey.equals(key)) {
            updatePreferenceSummary(findPreference(key), activityTimeSummary);
        }

    }

    private void updatePreferenceSummary(Preference pref, String summary) {
        if (pref instanceof EditTextPreference) {
            EditTextPreference p = (EditTextPreference) pref;
            pref.setSummary(String.format(summary, p.getText()));
        }
    }
}

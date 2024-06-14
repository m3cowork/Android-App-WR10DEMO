package com.m3.wr10.demo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.util.Log;

import androidx.annotation.NonNull;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;


public class PreferenceFragment extends PreferenceFragmentCompat {
    int gCount = 0;

    SharedPreferences prefs;

    private ResultWindowFragment resultWindowFragment;

    public PreferenceFragment(ResultWindowFragment resultWindowFragment) {
        this.resultWindowFragment = resultWindowFragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.code_settings, rootKey);
        PreferenceManager.setDefaultValues(requireContext(), R.xml.code_settings, false);

        if (rootKey == null) {

            prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            prefs.registerOnSharedPreferenceChangeListener(prefsListener);

///            Log.e("M3Mobile", "===register===prefsListener======");
        }
    }

    SharedPreferences.OnSharedPreferenceChangeListener prefsListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
///                    if (isViisble()) {
                    boolean bValue = prefs.getBoolean( key, false );

///  Log.d("M3Mobile","==onSharedPreferenceChanged::key=="+key+"==value=="+bTemp+", gCount="+gCount);   ///        gCount++;
     LogWriter.d( "==key==" + key + ", ==value==" + bValue + ", gCount=" + gCount );

                    resultWindowFragment.handleSettingsChanged( key, bValue );
                }
            };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener);
    }
}

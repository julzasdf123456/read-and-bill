package com.lopez.julz.readandbill.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.Settings;

public class SettingsHelper extends AsyncTask<AppDatabase, Void, Settings> {

    private Context context;
    public Settings settings;

    public SettingsHelper(Context context) {
        this.context = context;
    }

    @Override
    protected Settings doInBackground(AppDatabase... appDatabases) {
        try {
            AppDatabase db = appDatabases[0];

            settings = db.settingsDao().getSettings();
        } catch (Exception e) {
            Log.e("ER_GET_SETTINGS", e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Settings settings) {
        super.onPostExecute(settings);
    }
}

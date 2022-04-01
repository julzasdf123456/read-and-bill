package com.lopez.julz.readandbill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.lopez.julz.readandbill.adapters.DownloadReadingListAdapter;
import com.lopez.julz.readandbill.api.RequestPlaceHolder;
import com.lopez.julz.readandbill.api.RetrofitBuilder;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.ReadingSchedules;
import com.lopez.julz.readandbill.dao.Settings;
import com.lopez.julz.readandbill.helpers.AlertHelpers;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;
import com.lopez.julz.readandbill.helpers.SettingsHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadReadingListActivity extends AppCompatActivity {

    public Toolbar downloadReadingListToolbar;

    public List<ReadingSchedules> readingSchedulesList;
    public DownloadReadingListAdapter readingListAdapter;
    public RecyclerView downloadReadingListRecyclerview;

    public RetrofitBuilder retrofitBuilder;
    private RequestPlaceHolder requestPlaceHolder;

    public AppDatabase db;
    public Settings settings;

    public String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_reading_list);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        downloadReadingListToolbar = findViewById(R.id.downloadReadingListToolbar);
        setSupportActionBar(downloadReadingListToolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        userId = getIntent().getExtras().getString("USERID");

        new FetchSettings().execute();
    }

    public void fetchDownloadableSchedules() {
        try {
            Call<List<ReadingSchedules>> readingSchedulesCall = requestPlaceHolder.getUndownloadedSchedules(userId);

            readingSchedulesList.clear();

            readingSchedulesCall.enqueue(new Callback<List<ReadingSchedules>>() {
                @Override
                public void onResponse(Call<List<ReadingSchedules>> call, Response<List<ReadingSchedules>> response) {
                    if (response.isSuccessful()) {
                        readingSchedulesList.addAll(response.body());
                        readingListAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(DownloadReadingListActivity.this, "An error occurred while fetching the schedules", Toast.LENGTH_SHORT).show();
                        Log.e("ERR_FETCH_DATA", response.errorBody() + "" + response.raw());
                    }
                }

                @Override
                public void onFailure(Call<List<ReadingSchedules>> call, Throwable t) {
                    Toast.makeText(DownloadReadingListActivity.this, "An error occurred while fetching the schedules", Toast.LENGTH_SHORT).show();
                    Log.e("ERR_FETCH_DATA", t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("ERR_FETCH_DWNLD_SCHED", e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class FetchSettings extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                settings = db.settingsDao().getSettings();
            } catch (Exception e) {
                Log.e("ERR_FETCH_SETTINGS", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (settings != null) {
                retrofitBuilder = new RetrofitBuilder(settings.getDefaultServer());
                requestPlaceHolder = retrofitBuilder.getRetrofit().create(RequestPlaceHolder.class);

                downloadReadingListRecyclerview = findViewById(R.id.downloadReadingListRecyclerview);
                readingSchedulesList = new ArrayList<>();
                readingListAdapter = new DownloadReadingListAdapter(readingSchedulesList, DownloadReadingListActivity.this, settings);
                downloadReadingListRecyclerview.setAdapter(readingListAdapter);
                downloadReadingListRecyclerview.setLayoutManager(new LinearLayoutManager(DownloadReadingListActivity.this));

                fetchDownloadableSchedules();
            } else {
                AlertHelpers.showMessageDialog(DownloadReadingListActivity.this, "Settings Not Initialized", "Failed to load settings. Go to settings and set all necessary parameters to continue.");
            }
        }
    }
}
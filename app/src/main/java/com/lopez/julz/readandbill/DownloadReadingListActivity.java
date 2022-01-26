package com.lopez.julz.readandbill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.lopez.julz.readandbill.adapters.DownloadReadingListAdapter;
import com.lopez.julz.readandbill.api.RequestPlaceHolder;
import com.lopez.julz.readandbill.api.RetrofitBuilder;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.ReadingSchedules;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;

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

    public String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_reading_list);

        downloadReadingListToolbar = findViewById(R.id.downloadReadingListToolbar);
        setSupportActionBar(downloadReadingListToolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        userId = getIntent().getExtras().getString("USERID");

        retrofitBuilder = new RetrofitBuilder();
        requestPlaceHolder = retrofitBuilder.getRetrofit().create(RequestPlaceHolder.class);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        downloadReadingListRecyclerview = findViewById(R.id.downloadReadingListRecyclerview);
        readingSchedulesList = new ArrayList<>();
        readingListAdapter = new DownloadReadingListAdapter(readingSchedulesList, this);
        downloadReadingListRecyclerview.setAdapter(readingListAdapter);
        downloadReadingListRecyclerview.setLayoutManager(new LinearLayoutManager(this));

        fetchDownloadableSchedules();
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
                        Log.e("ERR_FETCH_DATA", response.errorBody() + "");
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
}
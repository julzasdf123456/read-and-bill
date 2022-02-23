package com.lopez.julz.readandbill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.lopez.julz.readandbill.adapters.ReadingListAdapter;
import com.lopez.julz.readandbill.api.RequestPlaceHolder;
import com.lopez.julz.readandbill.api.RetrofitBuilder;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.DownloadedPreviousReadings;
import com.lopez.julz.readandbill.dao.ReadingSchedules;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;

import java.util.ArrayList;
import java.util.List;

public class ReadingListActivity extends AppCompatActivity {

    public Toolbar readingListToolbar;

    public RecyclerView readingListRecyclerview;
    public ReadingListAdapter readingListAdapter;
    public List<ReadingSchedules> readingSchedulesList;

    public RetrofitBuilder retrofitBuilder;
    private RequestPlaceHolder requestPlaceHolder;

    public AppDatabase db;

    public String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_list);

        userId = getIntent().getExtras().getString("USERID");

        readingListToolbar = findViewById(R.id.readingListToolbar);
        setSupportActionBar(readingListToolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        retrofitBuilder = new RetrofitBuilder();
        requestPlaceHolder = retrofitBuilder.getRetrofit().create(RequestPlaceHolder.class);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        readingListRecyclerview = findViewById(R.id.readingListRecyclerview);
        readingSchedulesList = new ArrayList<>();
        readingListAdapter = new ReadingListAdapter(readingSchedulesList, this, userId);
        readingListRecyclerview.setAdapter(readingListAdapter);
        readingListRecyclerview.setLayoutManager(new LinearLayoutManager(this));

        new GetDownloadedSchedules().execute();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class GetDownloadedSchedules extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                readingSchedulesList.addAll(db.readingSchedulesDao().getActiveSchedules());
            } catch (Exception e) {
                Log.e("ERR_GET_ACTV_SCHD", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            new RemoveFinishedReadings().execute();
        }
    }

    public class RemoveFinishedReadings extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                List<ReadingSchedules> listTmp = readingSchedulesList;
                int size = listTmp.size();
                List<String> stringList = new ArrayList<>();
                stringList.add("READ");
                for (int i=0; i<size; i++) {
                    ReadingSchedules readingSchedules = listTmp.get(i);
                    List<DownloadedPreviousReadings> dprList = db.downloadedPreviousReadingsDao().getAllUnread(readingSchedules.getServicePeriod(), readingSchedules.getAreaCode(), readingSchedules.getGroupCode());
//                    Log.e("TEST", dprList.size() +"");
                    if (dprList.size() > 0) {

                    } else {
                        readingSchedulesList.remove(i);
                    }
                }
            } catch (Exception e) {
                Log.e("ERR_REMOVE_FINISHED_RED", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            readingListAdapter.notifyDataSetChanged();
        }
    }
}
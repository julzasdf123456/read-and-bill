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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.lopez.julz.readandbill.adapters.AccountsListAdapter;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.DownloadedPreviousReadings;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;

import java.util.ArrayList;
import java.util.List;

public class UnbilledActivity extends AppCompatActivity {

    Toolbar toolbarUnbilled;
    public TextView unbilledTitle;

    public String userId, areaCode, groupCode, servicePeriod;

    public MaterialButton toggleMenu;
    public RecyclerView readingMonitorViewRecyclerView;
    public AccountsListAdapter accountsListAdapter;
    public List<DownloadedPreviousReadings> downloadedPreviousReadingsList;

    public AppDatabase db;

    public boolean unbilled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unbilled);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        toolbarUnbilled = findViewById(R.id.toolbarUnbilled);
        setSupportActionBar(toolbarUnbilled);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userId = getIntent().getExtras().getString("USERID");
        areaCode = getIntent().getExtras().getString("AREACODE");
        groupCode = getIntent().getExtras().getString("GROUPCODE");
        servicePeriod = getIntent().getExtras().getString("SERVICEPERIOD");

        unbilledTitle = findViewById(R.id.unBilledTitle);
        unbilledTitle.setText("Reading Monitor - Area " + areaCode + " | Day " + groupCode + " (" + ObjectHelpers.formatShortDate(servicePeriod) + ")");

        toggleMenu = findViewById(R.id.toggleMenu);
        downloadedPreviousReadingsList = new ArrayList<>();
        readingMonitorViewRecyclerView = findViewById(R.id.readingMonitorViewRecyclerView);
        accountsListAdapter = new AccountsListAdapter(downloadedPreviousReadingsList, UnbilledActivity.this, servicePeriod, userId);
        readingMonitorViewRecyclerView.setAdapter(accountsListAdapter);
        readingMonitorViewRecyclerView.setLayoutManager(new LinearLayoutManager(UnbilledActivity.this));

        toggleMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (unbilled) {
                    unbilled = false;
                } else {
                    unbilled = true;
                }
                new ToggleList().execute(unbilled);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new ToggleList().execute(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class ToggleList extends AsyncTask<Boolean, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            downloadedPreviousReadingsList.clear();
        }

        @Override
        protected Void doInBackground(Boolean... bool) {
            try {
                boolean cond = bool[0];
                if (cond) {
                    downloadedPreviousReadingsList.addAll(db.downloadedPreviousReadingsDao().getAllUnread(servicePeriod, areaCode, groupCode));
                } else {
                    downloadedPreviousReadingsList.addAll(db.downloadedPreviousReadingsDao().getAllRead(servicePeriod, areaCode, groupCode));
                }
            } catch (Exception e) {
                Log.e("ERR_GET_LIST", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            accountsListAdapter.notifyDataSetChanged();
            if (unbilled) {
                toggleMenu.setText("UNREAD (" + downloadedPreviousReadingsList.size() + ")");
            } else {
                toggleMenu.setText("READ (" + downloadedPreviousReadingsList.size() + ")");
            }
            super.onPostExecute(unused);
        }
    }
}
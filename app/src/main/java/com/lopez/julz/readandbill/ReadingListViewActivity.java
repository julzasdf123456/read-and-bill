package com.lopez.julz.readandbill;

import static com.lopez.julz.readandbill.helpers.ObjectHelpers.hasPermissions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonParser;
import com.lopez.julz.readandbill.adapters.AccountsListAdapter;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.DownloadedPreviousReadings;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.ArrayList;
import java.util.List;

public class ReadingListViewActivity extends AppCompatActivity {

    Toolbar toolbarReadingListView;

    public String userId, areaCode, groupCode, servicePeriod;

    public EditText search;
    public TextView readingListTitle;

    public RecyclerView readingListViewRecyclerView;
    public AccountsListAdapter accountsListAdapter;
    public List<DownloadedPreviousReadings> downloadedPreviousReadingsList;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    public AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_list_view);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        toolbarReadingListView = findViewById(R.id.toolbarReadingListView);
        setSupportActionBar(toolbarReadingListView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userId = getIntent().getExtras().getString("USERID");
        areaCode = getIntent().getExtras().getString("AREACODE");
        groupCode = getIntent().getExtras().getString("GROUPCODE");
        servicePeriod = getIntent().getExtras().getString("SERVICEPERIOD");

        search = findViewById(R.id.searchList);
        downloadedPreviousReadingsList = new ArrayList<>();
        readingListViewRecyclerView = findViewById(R.id.readingListViewRecyclerView);
        accountsListAdapter = new AccountsListAdapter(downloadedPreviousReadingsList, ReadingListViewActivity.this, servicePeriod, userId);
        readingListViewRecyclerView.setAdapter(accountsListAdapter);
        readingListViewRecyclerView.setLayoutManager(new LinearLayoutManager(ReadingListViewActivity.this));
        readingListTitle = findViewById(R.id.readingListTitle);
        readingListTitle.setText("Area " + areaCode + " | Day " + groupCode + " (" + ObjectHelpers.formatShortDate(servicePeriod) + ")");

        new GetReadingList().execute();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                new Search().execute(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reading_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.mapView) {
            Intent intent = new Intent(ReadingListViewActivity.this, ReadingConsoleActivity.class);
            intent.putExtra("USERID", userId);
            intent.putExtra("AREACODE", areaCode);
            intent.putExtra("GROUPCODE", groupCode);
            intent.putExtra("SERVICEPERIOD", servicePeriod);
            startActivity(intent);
        } else if (item.getItemId() == R.id.unBilled) {
            Intent intent = new Intent(ReadingListViewActivity.this, UnbilledActivity.class);
            intent.putExtra("USERID", userId);
            intent.putExtra("AREACODE", areaCode);
            intent.putExtra("GROUPCODE", groupCode);
            intent.putExtra("SERVICEPERIOD", servicePeriod);
            startActivity(intent);
        } else if (item.getItemId() == R.id.newMeterCapture) {
            Intent intent = new Intent(ReadingListViewActivity.this, NewMeterActivity.class);
            intent.putExtra("USERID", userId);
            intent.putExtra("AREACODE", areaCode);
            intent.putExtra("GROUPCODE", groupCode);
            intent.putExtra("SERVICEPERIOD", servicePeriod);
            startActivity(intent);
        } else if (item.getItemId() == R.id.capturedMeters) {
            Intent intent = new Intent(ReadingListViewActivity.this, NewCapturedMetersActivity.class);
            intent.putExtra("USERID", userId);
            intent.putExtra("AREACODE", areaCode);
            intent.putExtra("GROUPCODE", groupCode);
            intent.putExtra("SERVICEPERIOD", servicePeriod);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public class Search extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            downloadedPreviousReadingsList.clear();
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                if (strings[0] != null) {
                    String searchRegex = "%" + strings[0] + "%";
                    downloadedPreviousReadingsList.addAll(db.downloadedPreviousReadingsDao().getSearch(servicePeriod, areaCode, groupCode, searchRegex));
                } else {
                    downloadedPreviousReadingsList.addAll(db.downloadedPreviousReadingsDao().getAllFromSchedule(servicePeriod, areaCode, groupCode));
                }

            } catch (Exception e) {
                Log.e("ERR_GET_SEARCH", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            accountsListAdapter.notifyDataSetChanged();
        }
    }

    public class GetReadingList extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            downloadedPreviousReadingsList.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                downloadedPreviousReadingsList.addAll(db.downloadedPreviousReadingsDao().getAllFromSchedule(servicePeriod, areaCode, groupCode));
            } catch (Exception e) {
                Log.e("ERR_GET_LIST", e.getMessage());
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            accountsListAdapter.notifyDataSetChanged();
        }
    }
}
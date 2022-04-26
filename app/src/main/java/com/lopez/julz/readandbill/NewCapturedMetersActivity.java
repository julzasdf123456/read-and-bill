package com.lopez.julz.readandbill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.Readings;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;

import java.util.ArrayList;
import java.util.List;

public class NewCapturedMetersActivity extends AppCompatActivity {

    Toolbar toolbarCapturedMeters;

    public AppDatabase db;

    public ListView listviewCapturedMeters;
    public List<String> newMetersList;
    ArrayAdapter<String> arrayAdapter;

    public String userId, areaCode, groupCode, servicePeriod;

    public FloatingActionButton newMeterCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_captured_meters);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        toolbarCapturedMeters = findViewById(R.id.toolbarCapturedMeters);
        setSupportActionBar(toolbarCapturedMeters);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userId = getIntent().getExtras().getString("USERID");
        areaCode = getIntent().getExtras().getString("AREACODE");
        groupCode = getIntent().getExtras().getString("GROUPCODE");
        servicePeriod = getIntent().getExtras().getString("SERVICEPERIOD");
        newMetersList = new ArrayList<>();
        newMeterCapture = findViewById(R.id.newMeterCaptureBtn);

        listviewCapturedMeters = findViewById(R.id.listviewCapturedMeters);

        newMeterCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewCapturedMetersActivity.this, NewMeterActivity.class);
                intent.putExtra("USERID", userId);
                intent.putExtra("AREACODE", areaCode);
                intent.putExtra("GROUPCODE", groupCode);
                intent.putExtra("SERVICEPERIOD", servicePeriod);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetNewMeters().execute();
    }

    public class GetNewMeters extends AsyncTask<Void, Void, Void> {

        List<Readings> readingsList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            readingsList = new ArrayList<>();
            newMetersList.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                readingsList.addAll(db.readingsDao().getNewCapturedReadings(servicePeriod));
            } catch (Exception e) {
                Log.e("ERR_GET_NW_MTRS", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            try {
                for (Readings reading: readingsList) {
                    newMetersList.add("Meter No: " + reading.getFieldStatus() + " (" + reading.getKwhUsed() + " kWh)");
                }
                arrayAdapter = new ArrayAdapter<String>(NewCapturedMetersActivity.this, android.R.layout.simple_list_item_1, newMetersList);
                listviewCapturedMeters.setAdapter(arrayAdapter);
            } catch (Exception e) {
                Log.e("ERR_SET_LIST_MTRS", e.getMessage());
            }
        }
    }
}
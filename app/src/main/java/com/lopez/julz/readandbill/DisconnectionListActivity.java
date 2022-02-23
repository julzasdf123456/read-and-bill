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
import android.widget.TextView;

import com.lopez.julz.readandbill.adapters.DisconnectionListAdapter;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.DisconnectionList;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;

import java.util.ArrayList;
import java.util.List;

public class DisconnectionListActivity extends AppCompatActivity {

    public Toolbar toolbarDisconnectionList;
    public RecyclerView recyclerviewDisconnectionListEdit;
    public List<DisconnectionList> disconnectionLists;
    public DisconnectionListAdapter disconnectionListAdapter;

    public String userId, period, area;

    public TextView servicePeriod, areaCode;

    public AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disconnection_list);

        toolbarDisconnectionList = findViewById(R.id.toolbarDisconnectionList);
        setSupportActionBar(toolbarDisconnectionList);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        userId = getIntent().getExtras().getString("USERID");
        period = getIntent().getExtras().getString("PERIOD");
        area = getIntent().getExtras().getString("AREA");

        recyclerviewDisconnectionListEdit = findViewById(R.id.recyclerviewDisconnectionListEdit);
        disconnectionLists = new ArrayList<>();
        disconnectionListAdapter = new DisconnectionListAdapter(disconnectionLists, this, userId);
        recyclerviewDisconnectionListEdit.setAdapter(disconnectionListAdapter);
        recyclerviewDisconnectionListEdit.setLayoutManager(new LinearLayoutManager(this));

        servicePeriod = findViewById(R.id.servicePeriod);
        areaCode = findViewById(R.id.area);
        servicePeriod.setText(ObjectHelpers.formatShortDate(period));
        areaCode.setText("Area: " + area);
        new GetDisconnectionList().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetDisconnectionList().execute();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class GetDisconnectionList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            disconnectionLists.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                disconnectionLists.addAll(db.disconnectionListDao().getListBySchedule(period, area));
            } catch (Exception e) {
                Log.e("ERR_GET_DSCO_LIST", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            disconnectionListAdapter.notifyDataSetChanged();
        }
    }
}
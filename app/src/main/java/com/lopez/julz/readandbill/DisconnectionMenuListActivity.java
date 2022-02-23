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

import com.lopez.julz.readandbill.adapters.DisconnectionGroupListAdapter;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;
import com.lopez.julz.readandbill.objects.DisconnectionGroupList;

import java.util.ArrayList;
import java.util.List;

public class DisconnectionMenuListActivity extends AppCompatActivity {

    public RecyclerView disconnectionSchedRecyclerView;
    public List<DisconnectionGroupList> disconnectionGroupLists;
    public DisconnectionGroupListAdapter disconnectionGroupListAdapter;

    public Toolbar discoListMenuToolbar;

    public AppDatabase db;

    public String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disconnection_menu_list);

        discoListMenuToolbar = findViewById(R.id.discoListMenuToolbar);
        setSupportActionBar(discoListMenuToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userId = getIntent().getExtras().getString("USERID");

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        disconnectionSchedRecyclerView = findViewById(R.id.disconnectionSchedRecyclerView);
        disconnectionGroupLists = new ArrayList<>();
        disconnectionGroupListAdapter = new DisconnectionGroupListAdapter(disconnectionGroupLists, this, userId);
        disconnectionSchedRecyclerView.setAdapter(disconnectionGroupListAdapter);
        disconnectionSchedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        new GetSchedList().execute();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class GetSchedList extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                disconnectionGroupLists.addAll(db.disconnectionListDao().getGroupDownloaded());
            } catch (Exception e) {
                Log.e("ERR_GET_LIST_DSCO", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            disconnectionGroupListAdapter.notifyDataSetChanged();
        }
    }
}
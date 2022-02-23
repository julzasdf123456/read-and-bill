package com.lopez.julz.readandbill;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.lopez.julz.readandbill.adapters.DisconnectionListDownloadAdapter;
import com.lopez.julz.readandbill.api.RequestPlaceHolder;
import com.lopez.julz.readandbill.api.RetrofitBuilder;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.DisconnectionList;
import com.lopez.julz.readandbill.helpers.AlertHelpers;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadDisconnectionListActivity extends AppCompatActivity {

    public RecyclerView recyclerviewDisconnectionList;
    public DisconnectionListDownloadAdapter disconnectionListDownloadAdapter;
    public FloatingActionButton downloadDisconnectionListBtn;
    public CircularProgressIndicator downloadDisconnectionProgress;

    public RetrofitBuilder retrofitBuilder;
    private RequestPlaceHolder requestPlaceHolder;

    public AppDatabase db;

    public String area, period;

    public List<DisconnectionList> disconnectionListList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_disconnection_list);

        area = getIntent().getExtras().getString("AREA");
        period = getIntent().getExtras().getString("PERIOD");

        downloadDisconnectionListBtn = findViewById(R.id.downloadDisconnectionListBtn);
        recyclerviewDisconnectionList = findViewById(R.id.recyclerviewDisconnectionList);
        downloadDisconnectionProgress = findViewById(R.id.downloadDisconnectionProgress);
        disconnectionListList = new ArrayList<>();
        disconnectionListDownloadAdapter = new DisconnectionListDownloadAdapter(disconnectionListList, this);
        recyclerviewDisconnectionList.setAdapter(disconnectionListDownloadAdapter);
        recyclerviewDisconnectionList.setLayoutManager(new LinearLayoutManager(this));

        downloadDisconnectionListBtn.setEnabled(false);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        retrofitBuilder = new RetrofitBuilder();
        requestPlaceHolder = retrofitBuilder.getRetrofit().create(RequestPlaceHolder.class);

        loadDisconnectionList();

        downloadDisconnectionListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SaveDisconnectionList().execute();
            }
        });
    }

    public void loadDisconnectionList() {
        try {
            Call<List<DisconnectionList>> disconnectionListCall = requestPlaceHolder.getDisconnectionList(period, area);

            disconnectionListCall.enqueue(new Callback<List<DisconnectionList>>() {
                @Override
                public void onResponse(Call<List<DisconnectionList>> call, Response<List<DisconnectionList>> response) {
                    if (response.isSuccessful()) {
                        downloadDisconnectionProgress.setVisibility(View.GONE);

                        new CheckDownloaded().execute(response.body());
                    } else {
                        Log.e("ERR_LOAD_DSCO_LST", response.message() + "\n" + response.raw());
                        AlertHelpers.showMessageDialog(DownloadDisconnectionListActivity.this, "Error Getting Disconnection List", response.message() + "\n" + response.raw());
                    }
                }

                @Override
                public void onFailure(Call<List<DisconnectionList>> call, Throwable t) {
                    Log.e("ERR_LOAD_DSCO_LST", t.getMessage());
                    AlertHelpers.showMessageDialog(DownloadDisconnectionListActivity.this, "Error Getting Disconnection List", t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("ERR_LOAD_DSCO_LST", e.getMessage());
            AlertHelpers.showMessageDialog(this, "Error Getting Disconnection List", e.getMessage());
        }
    }

    public class CheckDownloaded extends AsyncTask<List<DisconnectionList>, Void, Void> {

        @Override
        protected Void doInBackground(List<DisconnectionList>... lists) {
            try {
                if (lists != null) {
                    List<DisconnectionList> tmpList = lists[0];
                    for(int i=0; i<tmpList.size(); i++) {
                        DisconnectionList disconnectionList = db.disconnectionListDao().getOneByBillId(tmpList.get(i).getBillId());

                        if (disconnectionList != null) {

                        } else {
                            tmpList.get(i).setId(ObjectHelpers.getTimeInMillis() + "-" + ObjectHelpers.generateRandomString());
                            disconnectionListList.add(tmpList.get(i));
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("ERR_CHECK_DSCO_DL", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            disconnectionListDownloadAdapter.notifyDataSetChanged();
            downloadDisconnectionListBtn.setEnabled(true);
        }
    }

    public class SaveDisconnectionList extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (disconnectionListList != null) {
                    for (int i=0; i<disconnectionListList.size(); i++) {
                        db.disconnectionListDao().insertAll(disconnectionListList.get(i));
                    }
                }
            } catch (Exception e) {
                Log.e("ERR_SAVE_DSCO_LIST", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            finish();
            Toast.makeText(DownloadDisconnectionListActivity.this, "Disconnection List Downloaded", Toast.LENGTH_SHORT).show();
        }
    }
}
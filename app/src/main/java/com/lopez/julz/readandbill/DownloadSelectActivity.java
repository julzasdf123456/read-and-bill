package com.lopez.julz.readandbill;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.lopez.julz.readandbill.adapters.DownloadTrackAdapter;
import com.lopez.julz.readandbill.api.RequestPlaceHolder;
import com.lopez.julz.readandbill.api.RetrofitBuilder;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.TrackNames;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadSelectActivity extends AppCompatActivity {

    public RecyclerView downloadableTracks;
    public List<TrackNames> trackNamesList;
    public DownloadTrackAdapter downloadTrackAdapter;

    public Toolbar toolbarDownloadTracks;

    public RetrofitBuilder retrofitBuilder;
    private RequestPlaceHolder requestPlaceHolder;

    public AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_select);

        toolbarDownloadTracks = findViewById(R.id.toolbarDownloadTracks);
        setSupportActionBar(toolbarDownloadTracks);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        retrofitBuilder = new RetrofitBuilder();
        requestPlaceHolder = retrofitBuilder.getRetrofit().create(RequestPlaceHolder.class);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        downloadableTracks = findViewById(R.id.downloadableTracks);
        trackNamesList = new ArrayList<>();
        downloadTrackAdapter = new DownloadTrackAdapter(trackNamesList, this);
        downloadableTracks.setAdapter(downloadTrackAdapter);
        downloadableTracks.setLayoutManager(new LinearLayoutManager(this));
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
        fetchDownloadableTracks();
    }

    public void fetchDownloadableTracks() {
        try {
            trackNamesList.clear();
            Call<List<TrackNames>> listCall = requestPlaceHolder.getDownloadableTrackNames();

            listCall.enqueue(new Callback<List<TrackNames>>() {
                @Override
                public void onResponse(Call<List<TrackNames>> call, Response<List<TrackNames>> response) {
                    if (response.isSuccessful()) {
                        new VerifyExistingTrackNames().execute(response.body());
                    } else {
                        try {
                            Log.e("ER_FTCH_DWNDBLE_TRCKS_R", response.message() + " CODE " + response.code() + ", " + response.errorBody().string().toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<TrackNames>> call, Throwable t) {
                    Log.e("ER_FTCH_DWNDBLE_TRCKS_T", t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("ER_FTCH_DWNDBLE_TRCKS_E", e.getMessage());
        }
    }

    public class VerifyExistingTrackNames extends AsyncTask<List<TrackNames>, Void, Void> {

        @Override
        protected Void doInBackground(List<TrackNames>... lists) {
            try {
                if (lists != null) {
                    for(TrackNames trackNames : lists[0]) {
                        TrackNames tn = db.trackNamesDao().getOneByTrackName(trackNames.getTrackName());

                        if (tn != null) {

                        } else {
                            trackNamesList.add(trackNames);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("ERR_VERIF_EXIST_TRCK", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            downloadTrackAdapter.notifyDataSetChanged();
        }
    }
}
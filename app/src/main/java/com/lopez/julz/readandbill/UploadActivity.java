package com.lopez.julz.readandbill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.lopez.julz.readandbill.api.RequestPlaceHolder;
import com.lopez.julz.readandbill.api.RetrofitBuilder;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.TrackNames;
import com.lopez.julz.readandbill.dao.Tracks;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadActivity extends AppCompatActivity {

    public Toolbar toolbarUploadTracks;

    public MaterialButton uploadBtn;
    public LinearProgressIndicator progressBar;
    public TextView totalUploadableTracks, progress;

    public RetrofitBuilder retrofitBuilder;
    private RequestPlaceHolder requestPlaceHolder;

    public AppDatabase db;

    int totalCountTracks = 0;
    int progressUpload = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();
        retrofitBuilder = new RetrofitBuilder();
        requestPlaceHolder = retrofitBuilder.getRetrofit().create(RequestPlaceHolder.class);

        toolbarUploadTracks = findViewById(R.id.toolbarUploadTracks);
        setSupportActionBar(toolbarUploadTracks);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        uploadBtn = findViewById(R.id.uploadBtn);
        progressBar = findViewById(R.id.progressBar);
        totalUploadableTracks = findViewById(R.id.totalUploadableTracks);
        progress = findViewById(R.id.progress);

        uploadBtn.setEnabled(false);
        new InitializeUploadables().execute();

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UploadTrackNames().execute();
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

    public class InitializeUploadables extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                totalCountTracks = db.tracksDao().getCount();
            } catch (Exception e) {
                Log.e("ERR_INIT_TRCKS", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            totalUploadableTracks.setText("Tracks to Upload: " + totalCountTracks);
            progressBar.setMax(totalCountTracks);
            uploadBtn.setEnabled(true);
        }
    }

    public class UploadTrackNames extends AsyncTask<Void, Void, Void> {

        TrackNames trackNames;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setText("Initializing...");
            uploadBtn.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                trackNames = db.trackNamesDao().getOneByUnuploaded();
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            try {
                if (trackNames != null) {
                    Call<TrackNames> trackNamesCall = requestPlaceHolder.saveTrackNames(trackNames);

                    trackNamesCall.enqueue(new Callback<TrackNames>() {
                        @Override
                        public void onResponse(Call<TrackNames> call, Response<TrackNames> response) {
                            if (response.isSuccessful()) {
                                new UpdateTrackNamesUploaded().execute(trackNames);
                            } else {
                                try {
                                    Log.e("ERR_UPLD_TRCKNAME", response.message() + ", " + response.errorBody().string().toString());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<TrackNames> call, Throwable t) {
                            Log.e("ERR_UPLD_TRCKNAME", t.getMessage());
                        }
                    });
                } else {
                    new UploadTracks().execute();
//                    Toast.makeText(UploadActivity.this, "Upload finished!", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.e("ERR_UPLD_TRCKNAME", e.getMessage());
            }
        }
    }

    public class UpdateTrackNamesUploaded extends AsyncTask<TrackNames, Void, Void> {

        @Override
        protected Void doInBackground(TrackNames... trackNames) {
            try {
                if (trackNames != null) {
                    trackNames[0].setUploaded("Yes");
                    db.trackNamesDao().updateAll(trackNames[0]);
                }
            } catch (Exception e) {
                Log.e("ERR_UPDTE_TRCK_NME", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            new UploadTrackNames().execute();
        }
    }

    public class UploadTracks extends AsyncTask<Void, Void, Void> {

        Tracks tracks;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setText("Uploading: " + progressUpload + "/" + totalCountTracks);
            progressBar.setProgress(progressUpload);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                tracks = db.tracksDao().getOneByUnuploaded();
            } catch (Exception e) {
                Log.e("ERR_GET_TRCKS", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            try {
                if (tracks != null) {
                    Call<Tracks> tracksCall = requestPlaceHolder.saveTracks(tracks);

                    tracksCall.enqueue(new Callback<Tracks>() {
                        @Override
                        public void onResponse(Call<Tracks> call, Response<Tracks> response) {
                            if (response.isSuccessful()) {
                                new UpdateTrackUploaded().execute(tracks);
                                progressUpload += 1;
                            } else {
                                try {
                                    Log.e("ERR_UPLOD_TRCKS", response.message() + ", " + response.errorBody().string().toString());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Tracks> call, Throwable t) {
                            Log.e("ERR_UPLOD_TRCKS", t.getMessage());
                        }
                    });
                } else {
                    Toast.makeText(UploadActivity.this, "Upload finished!", Toast.LENGTH_LONG).show();
                    progress.setText("Upload complete!");
                }
            } catch (Exception e) {
                Log.e("ERR_UPLOD_TRCKS", e.getMessage());
            }
        }
    }

    public class UpdateTrackUploaded extends AsyncTask<Tracks, Void, Void> {

        @Override
        protected Void doInBackground(Tracks... tracks) {
            try {
                if (tracks != null) {
                    tracks[0].setUploaded("Yes");
                    db.tracksDao().updateAll(tracks[0]);
                }
            } catch (Exception e) {
                Log.e("ERR_UPDT_TRCKS_UPLDED", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            new UploadTracks().execute();
        }
    }
}
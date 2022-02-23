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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
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

public class UploadDisconnectionActivity extends AppCompatActivity {

    public Toolbar uploadToolbar;
    public TextView uploadableDiscoAccounts, uploadStatusText;
    public FloatingActionButton uploadButton;
    public CircularProgressIndicator uploadProgress;

    public AppDatabase db;

    public List<DisconnectionList> disconnectionLists;

    public RetrofitBuilder retrofitBuilder;
    private RequestPlaceHolder requestPlaceHolder;

    private int progress = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_disconnection);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        retrofitBuilder = new RetrofitBuilder();
        requestPlaceHolder = retrofitBuilder.getRetrofit().create(RequestPlaceHolder.class);

        uploadToolbar = findViewById(R.id.uploadToolbar);
        setSupportActionBar(uploadToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        uploadableDiscoAccounts = findViewById(R.id.uploadableDiscoAccounts);
        uploadStatusText = findViewById(R.id.uploadStatusText);
        uploadButton = findViewById(R.id.uploadButton);
        uploadProgress = findViewById(R.id.uploadProgress);
        disconnectionLists = new ArrayList<>();

        new FetchData().execute();

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadButton.setEnabled(false);
                uploadDisconnection();
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

    public class FetchData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                disconnectionLists.addAll(db.disconnectionListDao().getUploadable());
            } catch (Exception e) {
                Log.e("ERR_GET_UPLDBL_DSC", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            uploadableDiscoAccounts.setText("Disconnected Accounts: " + disconnectionLists.size());
            uploadProgress.setMax(disconnectionLists.size());
        }
    }

    public void uploadDisconnection() {
        try {
            if (disconnectionLists != null && disconnectionLists.size() > 0) {
                DisconnectionList disconnectionList = disconnectionLists.get(0);

                Call<Void> uploadCall = requestPlaceHolder.uploadDisconnection(disconnectionList);

                uploadCall.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            uploadProgress.setProgress(progress);

                            disconnectionLists.remove(0);

                            new UpdateDisconnectionStatus().execute(disconnectionList);

                            progress++;
                        } else {
                            Log.e("ERR_UPLOAD_DSCO", response.message() + "\n" + response.raw());
                            AlertHelpers.showMessageDialog(UploadDisconnectionActivity.this, "Upload Error", "An error occurred during the upload.\n" + response.message() + "\n" + response.raw());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("ERR_UPLOAD_DSCO", t.getMessage());
                        AlertHelpers.showMessageDialog(UploadDisconnectionActivity.this, "Upload Error", "An error occurred during the upload.\n" + t.getMessage());
                    }
                });
            } else {
                Toast.makeText(this, "Upload Complete", Toast.LENGTH_SHORT).show();
                uploadProgress.setProgress(0);
                uploadStatusText.setText("Upload Complete.");
            }
        } catch (Exception e) {
            Log.e("ERR_UPLOAD_DSCO", e.getMessage());
            AlertHelpers.showMessageDialog(this, "Upload Error", "An error occurred during the upload.\n" + e.getMessage());
        }
    }

    public class UpdateDisconnectionStatus extends AsyncTask<DisconnectionList, Void, Void> {

        @Override
        protected Void doInBackground(DisconnectionList... disconnectionLists) {
            try {
                if (disconnectionLists != null) {
                    DisconnectionList disconnectionListX = disconnectionLists[0];
                    if (disconnectionListX != null) {
                        disconnectionListX.setIsUploaded("Yes");
                        db.disconnectionListDao().updateAll(disconnectionListX);
                    }
                }
            } catch (Exception e) {
                Log.e("ERR_UPDT_DSCO", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            if (disconnectionLists != null && disconnectionLists.size() > 0) {
                uploadDisconnection();
            } else {
                Toast.makeText(UploadDisconnectionActivity.this, "Upload Complete", Toast.LENGTH_SHORT).show();
                uploadProgress.setProgress(0);
                uploadStatusText.setText("Upload Complete.");
            }
        }
    }
}
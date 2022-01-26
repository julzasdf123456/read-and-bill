package com.lopez.julz.readandbill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lopez.julz.readandbill.adapters.TrackNameAdapters;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.TrackNames;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;

import java.util.ArrayList;
import java.util.List;

public class CreateTracksActivity extends AppCompatActivity {

    public FloatingActionButton addTracks;
    public RecyclerView currentTracks;
    public TrackNameAdapters trackNameAdapters;
    public List<TrackNames> trackNamesList;

    public AppDatabase db;

    public Toolbar toolbarCreateTracks;

    private static final int WIFI_PERMISSION = 100;
    private static final int READ_PHONE_STATE = 101;
    private static final int ACCESS_LOCATION = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tracks);

        checkPermission(Manifest.permission.ACCESS_WIFI_STATE, WIFI_PERMISSION);
        checkPermission(Manifest.permission.READ_PHONE_STATE, READ_PHONE_STATE);
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_LOCATION);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        toolbarCreateTracks = findViewById(R.id.toolbarCreateTracks);
        setSupportActionBar(toolbarCreateTracks);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        addTracks = findViewById(R.id.addTracks);
        currentTracks = findViewById(R.id.currentTracks);
        trackNamesList = new ArrayList<>();
        trackNameAdapters = new TrackNameAdapters(trackNamesList, this);
        currentTracks.setAdapter(trackNameAdapters);
        currentTracks.setLayoutManager(new LinearLayoutManager(this));

        addTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateTracksActivity.this);

                EditText editText = new EditText(CreateTracksActivity.this);
                editText.setHint("Input Track Name");

                builder.setView(editText);

                builder.setTitle("Provide Track Name")
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (editText.getText().toString().isEmpty()) {
                                    Toast.makeText(CreateTracksActivity.this, "Please provide a name", Toast.LENGTH_SHORT).show();
                                } else {
                                    new SaveTrackName().execute(editText.getText().toString());
                                }
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();

                dialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetAllTrackNames().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_tracks_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.upload) {
            startActivity(new Intent(CreateTracksActivity.this, UploadActivity.class));
        } else if (item.getItemId() == R.id.download) {
            startActivity(new Intent(CreateTracksActivity.this, DownloadSelectActivity.class));
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class SaveTrackName extends AsyncTask<String, Void, Void> {

        boolean isExists = false;
        public String trckNm;

        @Override
        protected Void doInBackground(String... strings) {
            try {
                if (db.trackNamesDao().getOneByTrackName(strings[0]) != null) {
                    isExists = true;
                } else {
                    db.trackNamesDao().insertAll(new TrackNames(ObjectHelpers.getTimeInMillis(), strings[0], "No"));
                    trckNm = strings[0];
                    isExists = false;
                }

            } catch (Exception e) {
                Log.e("ERR_SV_TRCK_NAME", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            if (isExists) {
                Toast.makeText(CreateTracksActivity.this, "This track name already exists", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(CreateTracksActivity.this, RecordTracksActivity.class);
                intent.putExtra("TRACK_NAME", trckNm);
                startActivity(intent);
            }
        }
    }

    public class GetAllTrackNames extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            trackNamesList.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                trackNamesList.addAll(db.trackNamesDao().getAll());
            } catch (Exception e) {
                Log.e("ERR_GET_TRCKS", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            trackNameAdapters.notifyDataSetChanged();
        }
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(CreateTracksActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(CreateTracksActivity.this, new String[] { permission }, requestCode);
        } else {
//            Toast.makeText(LoginActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WIFI_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(CreateTracksActivity.this, "Wi-Fi Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(CreateTracksActivity.this, "Wi-Fi Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        } else if (requestCode == READ_PHONE_STATE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(CreateTracksActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CreateTracksActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == ACCESS_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(CreateTracksActivity.this, "Location Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CreateTracksActivity.this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
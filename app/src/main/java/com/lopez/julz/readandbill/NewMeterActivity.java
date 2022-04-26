package com.lopez.julz.readandbill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.Readings;
import com.lopez.julz.readandbill.helpers.AlertHelpers;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;

import java.util.List;

public class NewMeterActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    Toolbar toolbarNewMeter;

    public FloatingActionButton saveNewMeterReading;

    public String userId, areaCode, groupCode, servicePeriod;

    public EditText newMeter, prevKwh, presKwh, notes;

    public AppDatabase db;

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1002;

    /**
     * MAP
     */
    public MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private LocationComponent locationComponent;
    public Style style;
    public SymbolManager symbolManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_new_meter);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        toolbarNewMeter = findViewById(R.id.toolbarNewMeter);
        setSupportActionBar(toolbarNewMeter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userId = getIntent().getExtras().getString("USERID");
        areaCode = getIntent().getExtras().getString("AREACODE");
        groupCode = getIntent().getExtras().getString("GROUPCODE");
        servicePeriod = getIntent().getExtras().getString("SERVICEPERIOD");

        saveNewMeterReading = findViewById(R.id.saveNewMeterReading);
        newMeter = findViewById(R.id.meterNumberNew);
        prevKwh = findViewById(R.id.prevReadingNew);
        presKwh = findViewById(R.id.presReadingNew);
        notes = findViewById(R.id.notesNew);
        mapView = findViewById(R.id.mapviewCaptureMeter);

        newMeter.requestFocus();

        // MAP
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        saveNewMeterReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object presReadingInput = presKwh.getText();
                Object newMeterNo = newMeter.getText();

                if (presReadingInput != null && newMeterNo != null) {
                    Readings reading = new Readings();
                    reading.setId(ObjectHelpers.generateIDandRandString());
                    reading.setReadingTimestamp(ObjectHelpers.getCurrentTimestamp());
                    reading.setKwhUsed(presReadingInput.toString());
                    reading.setFieldStatus(newMeterNo.toString());
                    reading.setNotes(notes.getText().toString());
                    reading.setMeterReader(userId);
                    reading.setServicePeriod(servicePeriod);
                    reading.setUploadStatus("UPLOADABLE");
                    if (locationComponent != null) {
                        try {
                            reading.setLatitude(locationComponent.getLastKnownLocation().getLatitude() + "");
                            reading.setLongitude(locationComponent.getLastKnownLocation().getLongitude() + "");
                        } catch (Exception e) {
                            Log.e("ERR_GET_LOC", e.getMessage());
                        }
                    }
                    new SaveNewMeter().execute(reading);
                } else {
                    AlertHelpers.showMessageDialog(NewMeterActivity.this, "Incomplete Details", "Kindly supply the METER NUMBER and PRESENT READING field to continue");
                }
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
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        try {
            this.mapboxMap = mapboxMap;
            mapboxMap.setStyle(new Style.Builder()
                    .fromUri(getResources().getString(R.string.mapbox_style)), new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    setStyle(style);

                    enableLocationComponent(style);
                }
            });
        } catch (Exception e) {
            Log.e("ERR_INIT_MAPBOX", e.getMessage());
        }
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        try {
            // Check if permissions are enabled and if not request
            if (PermissionsManager.areLocationPermissionsGranted(this)) {

                // Get an instance of the component
                locationComponent = mapboxMap.getLocationComponent();

                // Activate with options
                locationComponent.activateLocationComponent(
                        LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

                // Enable to make component visible
                locationComponent.setLocationComponentEnabled(true);

                // Set the component's camera mode
                locationComponent.setCameraMode(CameraMode.TRACKING);

                // Set the component's render mode
                locationComponent.setRenderMode(RenderMode.COMPASS);

            } else {
                permissionsManager = new PermissionsManager(this);
                permissionsManager.requestLocationPermissions(this);
            }
        } catch (Exception e) {
            Log.e("ERR_LOAD_MAP", e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "READ_PHONE_STATE Denied", Toast.LENGTH_SHORT)
                            .show();
                } else {

                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int res = checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE);
            if (res != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE}, 123);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public class SaveNewMeter extends AsyncTask<Readings, Void, Void> {

        @Override
        protected Void doInBackground(Readings... readings) {
            try {
                if (readings != null) {
                    db.readingsDao().insertAll(readings[0]);
                }
            } catch (Exception e) {
                Log.e("ERR_SV_NEW_MTER", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            finish();
            Toast.makeText(NewMeterActivity.this, "New meter captured and saved!", Toast.LENGTH_SHORT).show();
        }
    }
}
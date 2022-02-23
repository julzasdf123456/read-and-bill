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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonParser;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.DisconnectionList;
import com.lopez.julz.readandbill.dao.DownloadedPreviousReadings;
import com.lopez.julz.readandbill.helpers.AlertHelpers;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.List;

public class DisconnectionFormActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    public TextView accountName, accountNo;
    public TextView sequenceCode, accountType, rate, amountDue, dateFrom, dateTo, dueDate;

    public  RadioButton disconnectButton;

    public FloatingActionButton saveBtn;

    public MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private LocationComponent locationComponent;
    public Style style;
    public SymbolManager symbolManager;

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1002;

    public Toolbar toolbarDisconnectionForm;

    public String userId, acctNo, period;

    public DisconnectionList disconnectionList;

    public AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_disconnection_form);

        Bundle bundle = getIntent().getExtras();
        userId = bundle.getString("USERID");
        acctNo = bundle.getString("ACCTNO");
        period = bundle.getString("PERIOD");

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        toolbarDisconnectionForm = findViewById(R.id.toolbarDisconnectionForm);
        setSupportActionBar(toolbarDisconnectionForm);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        accountName = findViewById(R.id.accountName);
        accountNo = findViewById(R.id.accountNo);
        sequenceCode = findViewById(R.id.sequenceCode);
        accountType = findViewById(R.id.accountType);
        rate = findViewById(R.id.rate);
        amountDue = findViewById(R.id.amountDue);
        dateFrom = findViewById(R.id.dateFrom);
        dateTo = findViewById(R.id.dateTo);
        dueDate = findViewById(R.id.dueDate);
        disconnectButton = findViewById(R.id.disconnectButton);
        saveBtn = findViewById(R.id.saveDisconnectionData);
        mapView = findViewById(R.id.mapviewDisconnectionForm);

        // MAP
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        
        new GetDetails().execute();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (disconnectButton.isChecked()) {
                    disconnectionList.setAccountStatus("DISCONNECTED");
                    disconnectionList.setIsUploaded("UPLOADABLE");

                    if (locationComponent != null) {
                        try {
                            disconnectionList.setLatitudeCaptured(locationComponent.getLastKnownLocation().getLatitude() + "");
                            disconnectionList.setLongitudeCaptured(locationComponent.getLastKnownLocation().getLongitude() + "");
                        } catch (Exception e) {
                            Log.e("ERR_GET_LOC", e.getMessage());
                        }
                    }

                    disconnectionList.setDateDisconnected(ObjectHelpers.getCurrentDate());
                    disconnectionList.setTimeDisconnected(ObjectHelpers.getCurrentTime());
                    disconnectionList.setUserId(userId);
                } else {
                    disconnectionList.setAccountStatus(null);
                    disconnectionList.setIsUploaded(null);
                }

                new SaveDisconnectionData().execute();
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

                    plotMarker();

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
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public boolean hasLatLong(DisconnectionList disconnectionList) {
        if (disconnectionList.getLatitude() != null) {
            return true;
        } else {
            return false;
        }
    }

    public void plotMarker() {
        try {
            if (symbolManager != null) {
                symbolManager.deleteAll();
            }
            symbolManager = new SymbolManager(mapView, mapboxMap, style);

            symbolManager.setIconAllowOverlap(true);
            symbolManager.setTextAllowOverlap(true);

            if (disconnectionList != null) {
                /**
                 * PLOT TO MAP
                 */
                if (hasLatLong(disconnectionList)) {
                    SymbolOptions symbolOptions;
                    symbolOptions = new SymbolOptions()
                            .withLatLng(new LatLng(Double.valueOf(disconnectionList.getLatitude()), Double.valueOf(disconnectionList.getLongitude())))
                            .withData(new JsonParser().parse("{" +
                                    "'id' : '" + disconnectionList.getId() + "'," +
                                    "'svcPeriod' : '" + disconnectionList.getServicePeriod() + "'}"))
                            .withIconImage("place-black-24dp")
                            .withIconSize(1f);

                    Symbol symbol = symbolManager.create(symbolOptions);

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(Double.valueOf(disconnectionList.getLatitude()), Double.valueOf(disconnectionList.getLongitude())))
                            .zoom(13)
                            .build();

                    if (mapboxMap != null) {
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1200);
                    } else {
                        Toast.makeText(DisconnectionFormActivity.this, "Map is still loading, try again in a couple of seconds", Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("ERR_PLOT_MRKER", e.getMessage());
        }
    }

    public class GetDetails extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                disconnectionList = db.disconnectionListDao().getOne(acctNo, period);
            } catch (Exception e) {
                Log.e("ERR_GET_DETAILS", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            if (disconnectionList != null) {
                accountName.setText(disconnectionList.getServiceAccountName());
                accountNo.setText(acctNo);
                sequenceCode.setText(disconnectionList.getSequenceCode());
                accountType.setText(disconnectionList.getConsumerType());
                rate.setText(ObjectHelpers.roundFour(Double.valueOf(disconnectionList.getEffectiveRate())));
                amountDue.setText(ObjectHelpers.roundTwo(Double.valueOf(disconnectionList.getNetAmount())));
                dateFrom.setText(ObjectHelpers.formatShortDateWithDate(disconnectionList.getServiceDateFrom()));
                dateTo.setText(ObjectHelpers.formatShortDateWithDate(disconnectionList.getServiceDateTo()));
                dueDate.setText(ObjectHelpers.formatShortDateWithDate(disconnectionList.getDueDate()));

                if (disconnectionList.getIsUploaded() != null && disconnectionList.getIsUploaded().equals("UPLOADABLE")) {
                    saveBtn.setEnabled(false);
                    disconnectButton.setChecked(true);
                } else {
                    saveBtn.setEnabled(true);
                    disconnectButton.setChecked(false);
                }
            } else {
                AlertHelpers.showMessageDialog(DisconnectionFormActivity.this, "No data found!", "No data found for this account!");
            }
        }
    }

    public class SaveDisconnectionData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... Void) {
            try {
                db.disconnectionListDao().updateAll(disconnectionList);
            } catch (Exception e) {
                Log.e("ERR_SV_DSCO_DATA", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            finish();
        }
    }
}
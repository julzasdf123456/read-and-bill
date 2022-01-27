package com.lopez.julz.readandbill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonParser;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.DownloadedPreviousReadings;
import com.lopez.julz.readandbill.dao.Rates;
import com.lopez.julz.readandbill.dao.Readings;
import com.lopez.julz.readandbill.helpers.AlertHelpers;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;
import com.lopez.julz.readandbill.helpers.ReadingHelpers;
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

public class ReadingFormActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    public Toolbar toolbarReadingForm;
    public TextView accountName, accountNumber;

    /**
     * BUNDLES
     */
    public String id, servicePeriod;

    public AppDatabase db;

    public DownloadedPreviousReadings currentDpr;
    public Rates currentRate;
    public Readings currentReading;

    /**
     * FORM
     */
    public EditText prevReading, presReading, notes;
    public TextView kwhUsed, accountType, rate, sequenceCode, accountStatus;
    public MaterialButton billBtn, nextBtn, prevBtn;

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
        setContentView(R.layout.activity_reading_form);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        toolbarReadingForm = findViewById(R.id.toolbarReadingForm);
        setSupportActionBar(toolbarReadingForm);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        id = getIntent().getExtras().getString("ID");
        servicePeriod = getIntent().getExtras().getString("SERVICEPERIOD");

        accountName = findViewById(R.id.accountName);
        accountNumber = findViewById(R.id.accountNumber);
        prevReading = findViewById(R.id.prevReading);
        presReading = findViewById(R.id.presReading);
        kwhUsed = findViewById(R.id.kwhUsed);
        billBtn = findViewById(R.id.billBtn);
        prevBtn = findViewById(R.id.prevButton);
        nextBtn = findViewById(R.id.nextButton);
        accountType = findViewById(R.id.accountType);
        rate = findViewById(R.id.rate);
        sequenceCode = findViewById(R.id.sequenceCode);
        notes = findViewById(R.id.notes);
        accountStatus = findViewById(R.id.accountStatus);

        // MAP
        mapView = findViewById(R.id.mapviewReadingForm);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FetchAccount().execute("next", currentDpr.getSequenceCode());
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FetchAccount().execute("prev", currentDpr.getSequenceCode());
            }
        });

        billBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Object presReadingInput = presReading.getText();
                    if (presReadingInput != null) {
                        Double kwhConsumed = Double.valueOf(ReadingHelpers.getKwhUsed(currentDpr, Double.valueOf(presReadingInput.toString())));

                        if (kwhConsumed < 0) {
                            AlertHelpers.showMessageDialog(ReadingFormActivity.this, "Invalid Input", "Present reading must not be less than the previous reading. Kindly check your input and try again.");
                        } else {
                            if (kwhConsumed > (Double.valueOf(currentDpr.getKwhUsed()) * 2)) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ReadingFormActivity.this);
                                builder.setTitle("WARNING")
                                        .setMessage("This consumer's power usage has increased by " + ObjectHelpers.roundTwo(((kwhConsumed / Double.valueOf(currentDpr.getKwhUsed())) * 100)) + "%. Do you wish to proceed?")
                                        .setNegativeButton("REVIEW READING", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .setPositiveButton("PROCEED", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                /**
                                                 * SAVE AND BILL
                                                 */
                                                Readings reading = new Readings();
                                                reading.setId(ObjectHelpers.getTimeInMillis() + "-" + ObjectHelpers.generateRandomString());
                                                reading.setAccountNumber(currentDpr.getId());
                                                reading.setServicePeriod(servicePeriod);
                                                reading.setReadingTimestamp(ObjectHelpers.getCurrentTimestamp());
                                                reading.setKwhUsed(presReadingInput.toString());
                                                reading.setNotes("DRASTIC INCREASE OF USAGE");
                                                if (locationComponent != null) {
                                                    reading.setLatitude(locationComponent.getLastKnownLocation().getLatitude() + "");
                                                    reading.setLongitude(locationComponent.getLastKnownLocation().getLongitude() + "");
                                                }
                                                new ReadAndBill().execute(reading);
                                                Toast.makeText(ReadingFormActivity.this, "Billed successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                builder.create().show();
                            } else {
                                /**
                                 * SAVE AND BILL
                                 */
                                Readings reading = new Readings();
                                reading.setId(ObjectHelpers.getTimeInMillis() + "-" + ObjectHelpers.generateRandomString());
                                reading.setAccountNumber(currentDpr.getId());
                                reading.setServicePeriod(servicePeriod);
                                reading.setReadingTimestamp(ObjectHelpers.getCurrentTimestamp());
                                reading.setKwhUsed(presReadingInput.toString());
                                if (locationComponent != null) {
                                    reading.setLatitude(locationComponent.getLastKnownLocation().getLatitude() + "");
                                    reading.setLongitude(locationComponent.getLastKnownLocation().getLongitude() + "");
                                }
                                new ReadAndBill().execute(reading);
                                Toast.makeText(ReadingFormActivity.this, "Billed successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(ReadingFormActivity.this, "No inputted present reading!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("ERR_COMP", e.getMessage());
                    Toast.makeText(ReadingFormActivity.this, "No inputted present reading!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        presReading.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (charSequence != null) {
                        Double kwh = Double.valueOf(ReadingHelpers.getKwhUsed(currentDpr, Double.valueOf(charSequence.toString())));
                        if (kwh < 0) {
                            kwhUsed.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_baseline_error_outline_18), null);
                        } else {
                            kwhUsed.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        }
                        kwhUsed.setText(ObjectHelpers.roundTwo(kwh));
                    } else {
                        kwhUsed.setText("");
                        kwhUsed.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    }
                } catch (Exception e) {
                    kwhUsed.setText("");
                    kwhUsed.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        new FetchInitID().execute(id);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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

    public class FetchInitID extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                currentDpr = db.downloadedPreviousReadingsDao().getOne(strings[0]);
                currentRate = db.ratesDao().getOne(currentDpr.getAccountType(), currentDpr.getAreaCode());
                currentReading = db.readingsDao().getOne(currentDpr.getId(), servicePeriod);
            } catch (Exception e) {
                Log.e("ERR_FETCH_NIT", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            accountName.setText(currentDpr.getServiceAccountName() != null ? currentDpr.getServiceAccountName() : "n/a");
            accountNumber.setText(currentDpr.getId() + " | " + currentDpr.getAccountType() + " | Seq. No. " + currentDpr.getSequenceCode());
            prevReading.setText(currentDpr.getKwhUsed()!=null ? currentDpr.getKwhUsed() : "0");
            accountType.setText(currentDpr.getAccountType());
            sequenceCode.setText(currentDpr.getSequenceCode());
            rate.setText(ObjectHelpers.roundFour(Double.parseDouble(currentRate.getTotalRateVATIncluded())));
            accountStatus.setText(currentDpr.getAccountStatus());

            if (currentDpr.getAccountStatus().equals("DISCONNECTED")) {
                billBtn.setEnabled(false);
            } else {
                billBtn.setEnabled(true);
            }

            /**
             * IF ALREADY READ
             */
            if (currentReading != null) {
                presReading.setText(currentReading.getKwhUsed());
                notes.setText(currentReading.getNotes());
                kwhUsed.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_baseline_check_circle_18), null, null, null);
            } else {
                presReading.setText("");
                notes.setText("");
                kwhUsed.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }
        }
    }

    public class FetchAccount extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            prevBtn.setEnabled(false);
            nextBtn.setEnabled(false);
            presReading.setText("");
        }

        @Override
        protected Void doInBackground(String... strings) { //strings[0] = next, prev | strings[1] = sequence
            String areaCode = currentDpr.getAreaCode();
            String groupCode = currentDpr.getGroupCode();
            if (strings[0].equals("prev")) {
                currentDpr = db.downloadedPreviousReadingsDao().getPrevious(Integer.valueOf(strings[1]), areaCode, groupCode);
                currentRate = db.ratesDao().getOne(currentDpr.getAccountType(), currentDpr.getAreaCode());
                currentReading = db.readingsDao().getOne(currentDpr.getId(), servicePeriod);

                if (currentDpr == null) {
                    currentDpr = db.downloadedPreviousReadingsDao().getLast(areaCode, groupCode);
                    currentRate = db.ratesDao().getOne(currentDpr.getAccountType(), currentDpr.getAreaCode());
                    currentReading = db.readingsDao().getOne(currentDpr.getId(), servicePeriod);
                }
            } else {
                currentDpr = db.downloadedPreviousReadingsDao().getNext(Integer.valueOf(strings[1]), areaCode, groupCode);
                currentRate = db.ratesDao().getOne(currentDpr.getAccountType(), currentDpr.getAreaCode());
                currentReading = db.readingsDao().getOne(currentDpr.getId(), servicePeriod);

                if (currentDpr == null) {
                    currentDpr = db.downloadedPreviousReadingsDao().getFirst(areaCode, groupCode);
                    currentRate = db.ratesDao().getOne(currentDpr.getAccountType(), currentDpr.getAreaCode());
                    currentReading = db.readingsDao().getOne(currentDpr.getId(), servicePeriod);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            accountName.setText(currentDpr.getServiceAccountName() != null ? currentDpr.getServiceAccountName() : "n/a");
            accountNumber.setText(currentDpr.getId());
            prevReading.setText(currentDpr.getKwhUsed()!=null ? currentDpr.getKwhUsed() : "0");
            accountType.setText(currentDpr.getAccountType());
            sequenceCode.setText(currentDpr.getSequenceCode());
            rate.setText(ObjectHelpers.roundFour(Double.parseDouble(currentRate.getTotalRateVATIncluded())));
            accountStatus.setText(currentDpr.getAccountStatus());

            prevBtn.setEnabled(true);
            nextBtn.setEnabled(true);

            if (currentDpr.getAccountStatus().equals("DISCONNECTED")) {
                billBtn.setEnabled(false);
            } else {
                billBtn.setEnabled(true);
            }

            plotMarker();

            /**
             * IF ALREADY READ
             */
            if (currentReading != null) {
                presReading.setText(currentReading.getKwhUsed());
                notes.setText(currentReading.getNotes());
                kwhUsed.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_baseline_check_circle_18), null, null, null);
            } else {
                presReading.setText("");
                notes.setText("");
                kwhUsed.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }
        }
    }

    public boolean hasLatLong(DownloadedPreviousReadings downloadedPreviousReadings) {
        if (downloadedPreviousReadings.getLatitude() != null) {
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

            if (currentDpr != null) {
                /**
                 * PLOT TO MAP
                 */
                if (hasLatLong(currentDpr)) {
                    SymbolOptions symbolOptions;
                    if (currentDpr.getAccountStatus().equals("ACTIVE")) {
                        symbolOptions = new SymbolOptions()
                                .withLatLng(new LatLng(Double.valueOf(currentDpr.getLatitude()), Double.valueOf(currentDpr.getLongitude())))
                                .withData(new JsonParser().parse("{" +
                                        "'id' : '" + currentDpr.getId() + "'," +
                                        "'svcPeriod' : '" + currentDpr.getServicePeriod() + "'}"))
                                .withIconImage("place-black-24dp")
                                .withIconSize(1f);
                    } else if (currentDpr.getAccountStatus().equals("DISCONNECTED")) {
                        symbolOptions = new SymbolOptions()
                                .withLatLng(new LatLng(Double.valueOf(currentDpr.getLatitude()), Double.valueOf(currentDpr.getLongitude())))
                                .withData(new JsonParser().parse("{" +
                                        "'id' : '" + currentDpr.getId() + "'," +
                                        "'svcPeriod' : '" + currentDpr.getServicePeriod() + "'}"))
                                .withIconImage("level-crossing")
                                .withIconSize(.50f);
                    } else {
                        symbolOptions = new SymbolOptions()
                                .withLatLng(new LatLng(Double.valueOf(currentDpr.getLatitude()), Double.valueOf(currentDpr.getLongitude())))
                                .withData(new JsonParser().parse("{" +
                                        "'id' : '" + currentDpr.getId() + "'," +
                                        "'svcPeriod' : '" + currentDpr.getServicePeriod() + "'}"))
                                .withIconImage("marker-blue")
                                .withIconSize(.50f);
                    }

                    Symbol symbol = symbolManager.create(symbolOptions);

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(Double.valueOf(currentDpr.getLatitude()), Double.valueOf(currentDpr.getLongitude())))
                            .zoom(13)
                            .build();

                    if (mapboxMap != null) {
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1200);
                    } else {
                        Toast.makeText(ReadingFormActivity.this, "Map is still loading, try again in a couple of seconds", Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("ERR_PLOT_MRKER", e.getMessage());
        }
    }

    /**
     * SAVE READING AND BILL AND PRINT
     */
    public class ReadAndBill extends AsyncTask<Readings, Void, Boolean> {
        String errors;

        @Override
        protected Boolean doInBackground(Readings... readings) {
            try {
                if (readings != null) {
                    Readings reading = readings[0];
                    if (reading != null) {
                        db.readingsDao().insertAll(reading);
                        currentDpr.setStatus("READ");
                        db.downloadedPreviousReadingsDao().updateAll(currentDpr);
                    }
                }
                return true;
            } catch (Exception e) {
                Log.e("ERR_READ_AND_BILL", e.getMessage());
                errors = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                /**
                 * PRINT BILL
                 */

                /**
                 * PROCEED TO NEXT
                 */
                new FetchAccount().execute("next", currentDpr.getSequenceCode());
            } else {
                AlertHelpers.showMessageDialog(ReadingFormActivity.this, "ERROR", "An error occurred while performing the reading. \n" + errors);
            }
        }
    }
}
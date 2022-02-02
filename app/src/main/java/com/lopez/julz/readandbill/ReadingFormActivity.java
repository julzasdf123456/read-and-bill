package com.lopez.julz.readandbill;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.content.FileProvider;
import androidx.room.Room;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonParser;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.Bills;
import com.lopez.julz.readandbill.dao.DownloadedPreviousReadings;
import com.lopez.julz.readandbill.dao.Rates;
import com.lopez.julz.readandbill.dao.ReadingImages;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReadingFormActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    public Toolbar toolbarReadingForm;
    public TextView accountName, accountNumber;

    /**
     * BUNDLES
     */
    public String id, servicePeriod, userId;

    public AppDatabase db;

    public DownloadedPreviousReadings currentDpr;
    public Rates currentRate;
    public Readings currentReading;
    public Bills currentBill;
    public Double kwhConsumed;
    public String readingId;

    /**
     * FORM
     */
    public EditText prevReading, presReading, notes;
    public TextView kwhUsed, accountType, rate, sequenceCode, accountStatus, coreloss, multiplier;
    public MaterialButton billBtn, nextBtn, prevBtn, takePhotoButton;
    public RadioGroup fieldStatus;

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

    /**
     * TAKE PHOTOS
     */
    static final int REQUEST_PICTURE_CAPTURE = 1;
    public FlexboxLayout imageFields;
    public String currentPhotoPath;

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
        userId = getIntent().getExtras().getString("USERID");

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
        coreloss = findViewById(R.id.coreloss);
        multiplier = findViewById(R.id.multiplier);
        fieldStatus = findViewById(R.id.fieldStatus);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        imageFields = findViewById(R.id.imageFields);

        fieldStatus.setVisibility(View.GONE);
        takePhotoButton.setVisibility(View.GONE);

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

        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        billBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Object presReadingInput = presReading.getText();
                    if (presReadingInput != null) {
                        kwhConsumed = Double.valueOf(ReadingHelpers.getKwhUsed(currentDpr, Double.valueOf(presReadingInput.toString())));

                        if (kwhConsumed < 0) {
                            AlertHelpers.showMessageDialog(ReadingFormActivity.this, "Invalid Input", "Present reading must not be less than the previous reading. Kindly check your input and try again.");
                        } else if (kwhConsumed == 0) {
                            /**
                             * SAVE AND BILL
                             */
                            Readings reading = new Readings();
                            reading.setId(readingId);
                            reading.setAccountNumber(currentDpr.getId());
                            reading.setServicePeriod(servicePeriod);
                            reading.setReadingTimestamp(ObjectHelpers.getCurrentTimestamp());
                            reading.setKwhUsed(presReadingInput.toString());
                            reading.setNotes("ZERO READING");
                            reading.setFieldStatus(ObjectHelpers.getSelectedTextFromRadioGroup(fieldStatus, getWindow().getDecorView()));
                            reading.setUploadStatus("UPLOADABLE");
                            reading.setReadingTimestamp(ObjectHelpers.getCurrentTimestamp());
                            if (locationComponent != null) {
                                try {
                                    reading.setLatitude(locationComponent.getLastKnownLocation().getLatitude() + "");
                                    reading.setLongitude(locationComponent.getLastKnownLocation().getLongitude() + "");
                                } catch (Exception e) {
                                    Log.e("ERR_GET_LOC", e.getMessage());
                                }
                            }
                            new ReadAndBill().execute(reading);
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
                                                reading.setId(readingId);
                                                reading.setAccountNumber(currentDpr.getId());
                                                reading.setServicePeriod(servicePeriod);
                                                reading.setReadingTimestamp(ObjectHelpers.getCurrentTimestamp());
                                                reading.setKwhUsed(presReadingInput.toString());
                                                reading.setNotes("DRASTIC INCREASE OF USAGE");
                                                reading.setFieldStatus("OVERREADING");
                                                reading.setUploadStatus("UPLOADABLE");
                                                reading.setReadingTimestamp(ObjectHelpers.getCurrentTimestamp());
                                                if (locationComponent != null) {
                                                    try {
                                                        reading.setLatitude(locationComponent.getLastKnownLocation().getLatitude() + "");
                                                        reading.setLongitude(locationComponent.getLastKnownLocation().getLongitude() + "");
                                                    } catch (Exception e) {
                                                        Log.e("ERR_GET_LOC", e.getMessage());
                                                    }
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
                                reading.setId(readingId);
                                reading.setAccountNumber(currentDpr.getId());
                                reading.setServicePeriod(servicePeriod);
                                reading.setReadingTimestamp(ObjectHelpers.getCurrentTimestamp());
                                reading.setKwhUsed(presReadingInput.toString());
                                reading.setNotes(notes.getText().toString());
                                reading.setUploadStatus("UPLOADABLE");
                                reading.setReadingTimestamp(ObjectHelpers.getCurrentTimestamp());
                                if (locationComponent != null) {
                                    try {
                                        reading.setLatitude(locationComponent.getLastKnownLocation().getLatitude() + "");
                                        reading.setLongitude(locationComponent.getLastKnownLocation().getLongitude() + "");
                                    } catch (Exception e) {
                                        Log.e("ERR_GET_LOC", e.getMessage());
                                    }
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
                            fieldStatus.setVisibility(View.GONE);
                            fieldStatus.clearCheck();
                            revealPhotoButton(false);
                        } else if (kwh == 0) {
                            /**
                             * SHOW OPTIONS FOR ZERO READING
                             */
                            fieldStatus.setVisibility(View.VISIBLE);
                            fieldStatus.check(R.id.stuckUp);
                            revealPhotoButton(true);
                        } else {
                            kwhUsed.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                            fieldStatus.setVisibility(View.GONE);
                            fieldStatus.clearCheck();
                            revealPhotoButton(false);
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
            readingId = ObjectHelpers.getTimeInMillis() + "-" + ObjectHelpers.generateRandomString();
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                currentDpr = db.downloadedPreviousReadingsDao().getOne(strings[0]);
                currentRate = db.ratesDao().getOne(currentDpr.getAccountType(), currentDpr.getAreaCode());
                currentReading = db.readingsDao().getOne(currentDpr.getId(), servicePeriod);
                currentBill = db.billsDao().getOneByAccountNumberAndServicePeriod(currentDpr.getId(), servicePeriod);
            } catch (Exception e) {
                Log.e("ERR_FETCH_NIT", e.getMessage());
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
            multiplier.setText(currentDpr.getMultiplier());
            coreloss.setText(currentDpr.getCoreloss());

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
                setSelectedStatus(currentReading.getFieldStatus());
                if (currentReading.getUploadStatus().equals("UPLOADED")) {
                    billBtn.setEnabled(false);
                } else {
                    billBtn.setEnabled(true);
                }
            } else {
                presReading.setText("");
                notes.setText("");
                kwhUsed.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }

            new GetPhotos().execute();
        }
    }

    public class FetchAccount extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            readingId = ObjectHelpers.getTimeInMillis() + "-" + ObjectHelpers.generateRandomString();
            prevBtn.setEnabled(false);
            nextBtn.setEnabled(false);
            presReading.setText("");
            fieldStatus.clearCheck();
            fieldStatus.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(String... strings) { //strings[0] = next, prev | strings[1] = sequence
            String areaCode = currentDpr.getAreaCode();
            String groupCode = currentDpr.getGroupCode();
            if (strings[0].equals("prev")) {
                currentDpr = db.downloadedPreviousReadingsDao().getPrevious(Integer.valueOf(strings[1]), areaCode, groupCode);

                if (currentDpr == null) {
                    currentDpr = db.downloadedPreviousReadingsDao().getLast(areaCode, groupCode);
                    currentRate = db.ratesDao().getOne(currentDpr.getAccountType(), currentDpr.getAreaCode());
                    currentReading = db.readingsDao().getOne(currentDpr.getId(), servicePeriod);
                    currentBill = db.billsDao().getOneByAccountNumberAndServicePeriod(currentDpr.getId(), servicePeriod);
                } else {
                    currentRate = db.ratesDao().getOne(currentDpr.getAccountType(), currentDpr.getAreaCode());
                    currentReading = db.readingsDao().getOne(currentDpr.getId(), servicePeriod);
                    currentBill = db.billsDao().getOneByAccountNumberAndServicePeriod(currentDpr.getId(), servicePeriod);
                }
            } else {
                currentDpr = db.downloadedPreviousReadingsDao().getNext(Integer.valueOf(strings[1]), areaCode, groupCode);

                if (currentDpr == null) {
                    currentDpr = db.downloadedPreviousReadingsDao().getFirst(areaCode, groupCode);
                    currentRate = db.ratesDao().getOne(currentDpr.getAccountType(), currentDpr.getAreaCode());
                    currentReading = db.readingsDao().getOne(currentDpr.getId(), servicePeriod);
                    currentBill = db.billsDao().getOneByAccountNumberAndServicePeriod(currentDpr.getId(), servicePeriod);
                } else {
                    currentRate = db.ratesDao().getOne(currentDpr.getAccountType(), currentDpr.getAreaCode());
                    currentReading = db.readingsDao().getOne(currentDpr.getId(), servicePeriod);
                    currentBill = db.billsDao().getOneByAccountNumberAndServicePeriod(currentDpr.getId(), servicePeriod);
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
            multiplier.setText(currentDpr.getMultiplier());
            coreloss.setText(currentDpr.getCoreloss());

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
                setSelectedStatus(currentReading.getFieldStatus());
                if (currentReading.getUploadStatus().equals("UPLOADED")) {
                    billBtn.setEnabled(false);
                } else {
                    billBtn.setEnabled(true);
                }
            } else {
                presReading.setText("");
                notes.setText("");
                kwhUsed.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }

            new GetPhotos().execute();
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
                        /** INSERT READING **/
                        db.readingsDao().insertAll(reading);

                        /** UPDATE STATUS OF DOWNLOADED READING **/
                        currentDpr.setStatus("READ");
                        db.downloadedPreviousReadingsDao().updateAll(currentDpr);

                        /** PERFORM BILLING **/
                        if (kwhConsumed == 0) {

                        } else {
                            if (currentBill != null) {
                                currentBill.setAccountNumber(currentDpr.getId());
                                currentBill.setServicePeriod(servicePeriod);
                                currentBill.setMultiplier(currentDpr.getMultiplier());
                                currentBill.setCoreloss(currentDpr.getCoreloss()!=null ? currentDpr.getCoreloss() : "0");
                                currentBill.setKwhUsed(kwhConsumed + "");
                                currentBill.setPreviousKwh(currentDpr.getKwhUsed());
                                currentBill.setPresentKwh(reading.getKwhUsed());
                                currentBill.setKwhAmount((Double.valueOf(currentRate.getTotalRateVATIncluded()) * kwhConsumed) + "");
                                currentBill.setEffectiveRate(currentRate.getTotalRateVATIncluded());
                                currentBill.setAdditionalCharges(null); // TO BE ADDED
                                currentBill.setDeductions(null); // TO BE ADDED

                                // GENERATE NET AMOUNT
                                double multiplier = currentDpr.getMultiplier() != null ? Double.valueOf(currentDpr.getMultiplier()) : 1;
                                double coreloss = currentDpr.getCoreloss() != null ? Double.valueOf(currentDpr.getCoreloss()) : 0;
                                double netAmount = ((kwhConsumed * multiplier) + coreloss) * Double.valueOf(currentRate.getTotalRateVATIncluded());
                                currentBill.setNetAmount(netAmount + "");
                                currentBill.setBillingDate(ObjectHelpers.getCurrentDate());
                                currentBill.setServiceDateFrom(ReadingHelpers.getServiceFromToday());
                                currentBill.setServiceDateTo(ReadingHelpers.getServiceTo());
                                currentBill.setDueDate(ReadingHelpers.getDueDate(servicePeriod));
                                currentBill.setMeterNumber(""); // TO BE ADDED
                                currentBill.setConsumerType(currentDpr.getAccountType());
                                currentBill.setBillType(currentBill.getConsumerType());

                                // COMPUTE RATES
                                currentBill.setGenerationSystemCharge(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getGenerationSystemCharge()))));
                                currentBill.setTransmissionDeliveryChargeKW(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getTransmissionDeliveryChargeKW()))));
                                currentBill.setTransmissionDeliveryChargeKWH(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getTransmissionDeliveryChargeKWH()))));
                                currentBill.setSystemLossCharge(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getSystemLossCharge()))));
                                currentBill.setDistributionDemandCharge(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getDistributionDemandCharge()))));
                                currentBill.setDistributionSystemCharge(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getDistributionSystemCharge()))));
                                currentBill.setSupplyRetailCustomerCharge(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getSupplyRetailCustomerCharge()))));
                                currentBill.setSupplySystemCharge(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getSupplySystemCharge()))));
                                currentBill.setMeteringRetailCustomerCharge(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getMeteringRetailCustomerCharge()))));
                                currentBill.setMeteringSystemCharge(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getMeteringSystemCharge()))));
                                currentBill.setRFSC(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getRFSC()))));
                                currentBill.setLifelineRate(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getLifelineRate()))));
                                currentBill.setInterClassCrossSubsidyCharge(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getInterClassCrossSubsidyCharge()))));
                                currentBill.setPPARefund(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getPPARefund()))));
                                currentBill.setSeniorCitizenSubsidy(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getSeniorCitizenSubsidy()))));
                                currentBill.setMissionaryElectrificationCharge(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getMissionaryElectrificationCharge()))));
                                currentBill.setEnvironmentalCharge(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getEnvironmentalCharge()))));
                                currentBill.setStrandedContractCosts(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getStrandedContractCosts()))));
                                currentBill.setNPCStrandedDebt(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getNPCStrandedDebt()))));
                                currentBill.setFeedInTariffAllowance(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getFeedInTariffAllowance()))));
                                currentBill.setMissionaryElectrificationREDCI(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getMissionaryElectrificationREDCI()))));
                                currentBill.setGenerationVAT(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getGenerationVAT()))));
                                currentBill.setTransmissionVAT(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getTransmissionVAT()))));
                                currentBill.setSystemLossVAT(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getSystemLossVAT()))));
                                currentBill.setDistributionVAT(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getDistributionVAT()))));
                                currentBill.setRealPropertyTax(ObjectHelpers.roundFour(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getRealPropertyTax()))));

                                currentBill.setUserId(userId);
                                currentBill.setBilledFrom("APP");
                                currentBill.setUploadStatus("UPLOADABLE");

                                db.billsDao().updateAll(currentBill);
                            } else {
                                currentBill = new Bills();
                                currentBill.setId(ObjectHelpers.getTimeInMillis() + "-" + ObjectHelpers.generateRandomString());
                                currentBill.setBillNumber(ReadingHelpers.generateBillNumber(currentDpr.getAreaCode()));
                                currentBill.setAccountNumber(currentDpr.getId());
                                currentBill.setServicePeriod(servicePeriod);
                                currentBill.setMultiplier(currentDpr.getMultiplier());
                                currentBill.setCoreloss(currentDpr.getCoreloss()!=null ? currentDpr.getCoreloss() : "0");
                                currentBill.setKwhUsed(kwhConsumed + "");
                                currentBill.setPreviousKwh(currentDpr.getKwhUsed());
                                currentBill.setPresentKwh(reading.getKwhUsed());
                                currentBill.setKwhAmount((Double.valueOf(currentRate.getTotalRateVATIncluded()) * kwhConsumed) + "");
                                currentBill.setEffectiveRate(currentRate.getTotalRateVATIncluded());
                                currentBill.setAdditionalCharges(null); // TO BE ADDED
                                currentBill.setDeductions(null); // TO BE ADDED

                                // GENERATE NET AMOUNT
                                double multiplier = currentDpr.getMultiplier() != null ? Double.valueOf(currentDpr.getMultiplier()) : 1;
                                double coreloss = currentDpr.getCoreloss() != null ? Double.valueOf(currentDpr.getCoreloss()) : 0;
                                double netAmount = ((kwhConsumed * multiplier) + coreloss) * Double.valueOf(currentRate.getTotalRateVATIncluded());
                                currentBill.setNetAmount(netAmount + "");
                                currentBill.setBillingDate(ObjectHelpers.getCurrentDate());
                                currentBill.setServiceDateFrom(ReadingHelpers.getServiceFromToday());
                                currentBill.setServiceDateTo(ReadingHelpers.getServiceTo());
                                currentBill.setDueDate(ReadingHelpers.getDueDate(servicePeriod));
                                currentBill.setMeterNumber(""); // TO BE ADDED
                                currentBill.setConsumerType(currentDpr.getAccountType());
                                currentBill.setBillType(currentBill.getConsumerType());

                                // COMPUTE RATES
                                currentBill.setGenerationSystemCharge(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getGenerationSystemCharge()))));
                                currentBill.setTransmissionDeliveryChargeKW(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getTransmissionDeliveryChargeKW()))));
                                currentBill.setTransmissionDeliveryChargeKWH(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getTransmissionDeliveryChargeKWH()))));
                                currentBill.setSystemLossCharge(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getSystemLossCharge()))));
                                currentBill.setDistributionDemandCharge(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getDistributionDemandCharge()))));
                                currentBill.setDistributionSystemCharge(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getDistributionSystemCharge()))));
                                currentBill.setSupplyRetailCustomerCharge(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getSupplyRetailCustomerCharge()))));
                                currentBill.setSupplySystemCharge(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getSupplySystemCharge()))));
                                currentBill.setMeteringRetailCustomerCharge(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getMeteringRetailCustomerCharge()))));
                                currentBill.setMeteringSystemCharge(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getMeteringSystemCharge()))));
                                currentBill.setRFSC(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getRFSC()))));
                                currentBill.setLifelineRate(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getLifelineRate()))));
                                currentBill.setInterClassCrossSubsidyCharge(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getInterClassCrossSubsidyCharge()))));
                                currentBill.setPPARefund(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getPPARefund()))));
                                currentBill.setSeniorCitizenSubsidy(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getSeniorCitizenSubsidy()))));
                                currentBill.setMissionaryElectrificationCharge(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getMissionaryElectrificationCharge()))));
                                currentBill.setEnvironmentalCharge(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getEnvironmentalCharge()))));
                                currentBill.setStrandedContractCosts(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getStrandedContractCosts()))));
                                currentBill.setNPCStrandedDebt(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getNPCStrandedDebt()))));
                                currentBill.setFeedInTariffAllowance(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getFeedInTariffAllowance()))));
                                currentBill.setMissionaryElectrificationREDCI(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getMissionaryElectrificationREDCI()))));
                                currentBill.setGenerationVAT(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getGenerationVAT()))));
                                currentBill.setTransmissionVAT(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getTransmissionVAT()))));
                                currentBill.setSystemLossVAT(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getSystemLossVAT()))));
                                currentBill.setDistributionVAT(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getDistributionVAT()))));
                                currentBill.setRealPropertyTax(ObjectHelpers.roundFourNoComma(kwhConsumed * Double.valueOf(ObjectHelpers.doubleStringNull(currentRate.getRealPropertyTax()))));

                                currentBill.setUserId(userId);
                                currentBill.setBilledFrom("APP");
                                currentBill.setUploadStatus("UPLOADABLE");

                                db.billsDao().insertAll(currentBill);
                            }
                        }

                    }
                }
                return true;
            } catch (Exception e) {
                Log.e("ERR_READ_AND_BILL", e.getMessage());
                e.printStackTrace();
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

    public void setSelectedStatus(String status) {
        try {
            if (status.equals("STUCK-UP")) {
                fieldStatus.check(R.id.stuckUp);
            } else if (status.equals("NOT IN USE")) {
                fieldStatus.check(R.id.notInUse);
            } else if (status.equals("NO DISPLAY")) {
                fieldStatus.check(R.id.noDisplay);
            } else {
                fieldStatus.clearCheck();
            }
        } catch (Exception e) {
            Log.e("ERR_SET_SEL", e.getMessage());
            fieldStatus.clearCheck();
        }
    }

    /**
     * TAKE PHOTOS
     */
    private void dispatchTakePictureIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra( MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);

            File pictureFile = null;
            try {
                pictureFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this,
                        "Photo file can't be created, please try again",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.lopez.julz.readandbill",
                        pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "READING_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile,  ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        new SavePhotoToDatabase().execute(currentPhotoPath);
        return image;
    }

    public class SavePhotoToDatabase extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                if (strings != null) {
                    String photo = strings[0];

                    ReadingImages photoObject = new ReadingImages(ObjectHelpers.getTimeInMillis() + "-" + ObjectHelpers.generateRandomString(), photo, null, servicePeriod, currentDpr.getId(), "UPLOADABLE");
                    db.readingImagesDao().insertAll(photoObject);
                }
            } catch (Exception e) {
                Log.e("ERR_SAVE_PHOTO_DB", e.getMessage());
            }

            return null;
        }
    }

    public class GetPhotos extends AsyncTask<Void, Void, Void> {

        List<ReadingImages> photosList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageFields.removeAllViews();
            photosList.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                photosList.addAll(db.readingImagesDao().getAll(servicePeriod, currentDpr.getId()));
            } catch (Exception e) {
                Log.e("ERR_GET_IMGS", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            if (photosList != null) {
                revealPhotoButton(false);
                for (int i = 0; i < photosList.size(); i++) {
                    File file = new File(photosList.get(i).getPhoto());
                    if (file.exists()) {
                        Log.e("TEST", file.getPath());
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                        Bitmap scaledBmp = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 8, bitmap.getHeight() / 8, true);
                        ImageView imageView = new ImageView(ReadingFormActivity.this);
                        Constraints.LayoutParams layoutParams = new Constraints.LayoutParams(scaledBmp.getWidth(), scaledBmp.getHeight());
                        imageView.setLayoutParams(layoutParams);
                        imageView.setPadding(0, 5, 5, 0);
                        imageView.setImageBitmap(scaledBmp);
                        imageFields.addView(imageView);

                        imageView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                PopupMenu popup = new PopupMenu(ReadingFormActivity.this, imageView);
                                //inflating menu from xml resource
                                popup.inflate(R.menu.image_menu);
                                //adding click listener
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.delete_img:
                                                file.delete();
                                                new GetPhotos().execute();
                                                return true;
                                            default:
                                                return false;
                                        }
                                    }
                                });
                                //displaying the popup
                                popup.show();
                                return false;
                            }
                        });
                    } else {
                        Log.e("ERR_RETRV_FILE", "Error retriveing file");
                    }
                }
            } else {
                revealPhotoButton(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICTURE_CAPTURE && resultCode == RESULT_OK) {
            File imgFile = new  File(currentPhotoPath);
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getPath());
            Bitmap scaledBmp = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/8, bitmap.getHeight()/8, true);

            ImageView imageView = new ImageView(ReadingFormActivity.this);
            Constraints.LayoutParams layoutParams = new Constraints.LayoutParams(scaledBmp.getWidth(), scaledBmp.getHeight());
            imageView.setLayoutParams(layoutParams);
            imageView.setPadding(0, 5, 5, 0);
            if (imgFile.exists()) {
                imageView.setImageBitmap(scaledBmp);
            }
            imageFields.addView(imageView);
            revealPhotoButton(false);

            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenu popup = new PopupMenu(ReadingFormActivity.this, imageView);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.image_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete_img:
                                    if (imgFile.exists()) {
                                        imgFile.delete();
                                        new GetPhotos().execute();
                                    }
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    //displaying the popup
                    popup.show();
                    return false;
                }
            });
        }
    }

    public void revealPhotoButton(boolean regex) {
        if (regex) {
            takePhotoButton.setVisibility(View.VISIBLE);
            billBtn.setVisibility(View.GONE);
        } else {
            takePhotoButton.setVisibility(View.GONE);
            billBtn.setVisibility(View.VISIBLE);
        }
    }
}
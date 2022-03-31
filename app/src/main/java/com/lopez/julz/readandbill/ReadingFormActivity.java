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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonParser;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.Bills;
import com.lopez.julz.readandbill.dao.DownloadedPreviousReadings;
import com.lopez.julz.readandbill.dao.Rates;
import com.lopez.julz.readandbill.dao.ReadingImages;
import com.lopez.julz.readandbill.dao.Readings;
import com.lopez.julz.readandbill.dao.Users;
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
import java.util.Set;

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
    public Users user;

    /**
     * FORM
     */
    public EditText prevReading, presReading, notes;
    public TextView kwhUsed, accountType, rate, sequenceCode, accountStatus, coreloss, multiplier, seniorCitizen, currentArrears, totalArrears;
    public MaterialButton billBtn, nextBtn, prevBtn, takePhotoButton, printBtn;
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

    /**
     * BT
     */
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
    Printer printer = null;

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
        seniorCitizen = findViewById(R.id.seniorCitizen);
        currentArrears = findViewById(R.id.currentArrears);
        totalArrears = findViewById(R.id.totalArrears);
        printBtn = findViewById(R.id.printBtn);

        printBtn.setVisibility(View.GONE);

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
                            reading.setMeterReader(userId);
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
                            String prevKwh = currentDpr.getKwhUsed() != null ? currentDpr.getKwhUsed() : "0";
                            if (kwhConsumed > (Double.valueOf(prevKwh) * 2)) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ReadingFormActivity.this);
                                builder.setTitle("WARNING")
                                        .setMessage("This consumer's power usage has increased by " + ObjectHelpers.roundTwo(((kwhConsumed / Double.valueOf(prevKwh)) * 100)) + "%. Do you wish to proceed?")
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
                                                reading.setMeterReader(userId);
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
                                reading.setMeterReader(userId);
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
                    e.printStackTrace();
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
                            takePhotoButton.setEnabled(true);
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

        printBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                print(currentBill, currentRate, currentDpr);
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

                // CONFIGURE CONSUMER TYPE
                if (currentDpr.getAccountType().equals("NONE")) {
                    currentDpr.setAccountType("RESIDENTIAL");
                }

                currentRate = db.ratesDao().getOne(ReadingHelpers.getAccountType(currentDpr), currentDpr.getTown());
                currentReading = db.readingsDao().getOne(currentDpr.getId(), servicePeriod);
                currentBill = db.billsDao().getOneByAccountNumberAndServicePeriod(currentDpr.getId(), servicePeriod);
                user = db.usersDao().getOneById(userId);
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
            accountType.setText(ReadingHelpers.getAccountType(currentDpr));
            sequenceCode.setText(currentDpr.getSequenceCode());
            rate.setText(currentRate.getTotalRateVATIncluded() != null ? (ObjectHelpers.roundFour(Double.parseDouble(currentRate.getTotalRateVATIncluded()))) : "0");
            accountStatus.setText(currentDpr.getAccountStatus());
            multiplier.setText(currentDpr.getMultiplier());
            coreloss.setText(currentDpr.getCoreloss());
            seniorCitizen.setText(currentDpr.getSeniorCitizen() != null ? currentDpr.getSeniorCitizen() : "No");
            currentArrears.setText(currentDpr.getArrearsLedger() != null ? ObjectHelpers.roundTwo(Double.valueOf(currentDpr.getArrearsLedger())) : "0.0");
            totalArrears.setText(currentDpr.getBalance() != null ? ObjectHelpers.roundTwo(Double.valueOf(currentDpr.getBalance())) : "0.0");

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
                    takePhotoButton.setEnabled(false);
                } else {
                    billBtn.setEnabled(true);
                    takePhotoButton.setEnabled(true);

                    /**
                     * SHOW TAKE PHOTO BUTTON
                     */
                    revealPhotoButton(true);
                }
            } else {
                presReading.setText("");
                notes.setText("");
                kwhUsed.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                revealPhotoButton(false);
            }

            /**
             * IF HAS BILL
             */
            if (currentBill != null) {
                printBtn.setVisibility(View.VISIBLE);
            } else {
                printBtn.setVisibility(View.GONE);
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
            String areaCode = currentDpr.getTown();
            String groupCode = currentDpr.getGroupCode();
            if (strings[0].equals("prev")) {
                currentDpr = db.downloadedPreviousReadingsDao().getPrevious(Integer.valueOf(strings[1]), areaCode, groupCode);

                if (currentDpr == null) {
                    currentDpr = db.downloadedPreviousReadingsDao().getLast(areaCode, groupCode);
                    currentRate = db.ratesDao().getOne(ReadingHelpers.getAccountType(currentDpr), currentDpr.getTown());
                    currentReading = db.readingsDao().getOne(currentDpr.getId(), servicePeriod);
                    currentBill = db.billsDao().getOneByAccountNumberAndServicePeriod(currentDpr.getId(), servicePeriod);
                } else {
                    currentRate = db.ratesDao().getOne(ReadingHelpers.getAccountType(currentDpr), currentDpr.getTown());
                    currentReading = db.readingsDao().getOne(currentDpr.getId(), servicePeriod);
                    currentBill = db.billsDao().getOneByAccountNumberAndServicePeriod(currentDpr.getId(), servicePeriod);
                }
            } else {
                currentDpr = db.downloadedPreviousReadingsDao().getNext(Integer.valueOf(strings[1]), areaCode, groupCode);

                if (currentDpr == null) {
                    currentDpr = db.downloadedPreviousReadingsDao().getFirst(areaCode, groupCode);
                    currentRate = db.ratesDao().getOne(ReadingHelpers.getAccountType(currentDpr), currentDpr.getTown());
                    currentReading = db.readingsDao().getOne(currentDpr.getId(), servicePeriod);
                    currentBill = db.billsDao().getOneByAccountNumberAndServicePeriod(currentDpr.getId(), servicePeriod);
                } else {
                    currentRate = db.ratesDao().getOne(ReadingHelpers.getAccountType(currentDpr), currentDpr.getTown());
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
            accountType.setText(ReadingHelpers.getAccountType(currentDpr));
            sequenceCode.setText(currentDpr.getSequenceCode());
            rate.setText(ObjectHelpers.roundFour(Double.parseDouble(currentRate.getTotalRateVATIncluded())));
            accountStatus.setText(currentDpr.getAccountStatus());
            multiplier.setText(currentDpr.getMultiplier());
            coreloss.setText(currentDpr.getCoreloss());
            seniorCitizen.setText(currentDpr.getSeniorCitizen() != null ? currentDpr.getSeniorCitizen() : "No");
            currentArrears.setText(currentDpr.getArrearsLedger() != null ? ObjectHelpers.roundTwo(Double.valueOf(currentDpr.getArrearsLedger())) : "0.0");
            totalArrears.setText(currentDpr.getBalance() != null ? ObjectHelpers.roundTwo(Double.valueOf(currentDpr.getBalance())) : "0.0");

            prevBtn.setEnabled(true);
            nextBtn.setEnabled(true);

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
                    takePhotoButton.setEnabled(false);
                } else {
                    billBtn.setEnabled(true);
                    takePhotoButton.setEnabled(true);

                    /**
                     * SHOW TAKE PHOTO BUTTON
                     */
                    revealPhotoButton(true);
                }
            } else {
                presReading.setText("");
                notes.setText("");
                kwhUsed.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                revealPhotoButton(false);
            }

            /**
             * IF HAS BILL
             */
            if (currentBill != null) {
                printBtn.setVisibility(View.VISIBLE);
            } else {
                printBtn.setVisibility(View.GONE);
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
                        if (currentReading != null) {
                            currentReading.setKwhUsed(reading.getKwhUsed());
                            currentReading.setNotes("PERFORMED RE-READING");
                            currentReading.setLatitude(reading.getLatitude());
                            currentReading.setLongitude(reading.getLongitude());
                            db.readingsDao().updateAll(currentReading);
                        } else {
                            db.readingsDao().insertAll(reading);
                        }

                        /** UPDATE STATUS OF DOWNLOADED READING **/
                        currentDpr.setStatus("READ");
                        db.downloadedPreviousReadingsDao().updateAll(currentDpr);

                        if (currentDpr.getAccountStatus() != null && currentDpr.getAccountStatus().equals("DISCONNECTED")) {

                        } else {
                            /** PERFORM BILLING **/
                            if (kwhConsumed == 0) {

                            } else {
                                if (currentBill != null) {
                                    currentBill = ReadingHelpers.generateRegularBill(currentBill, currentDpr, currentRate, kwhConsumed, Double.valueOf(reading.getKwhUsed()), userId);

                                    db.billsDao().updateAll(currentBill);
                                } else {
                                    currentBill = ReadingHelpers.generateRegularBill(null, currentDpr, currentRate, kwhConsumed, Double.valueOf(reading.getKwhUsed()), userId);

                                    db.billsDao().insertAll(currentBill);
                                }
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
                print(currentBill, currentRate, currentDpr);

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
//                revealPhotoButton(false);
                for (int i = 0; i < photosList.size(); i++) {
                    File file = new File(photosList.get(i).getPhoto());
                    if (file.exists()) {
                        try {
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
                        } catch (Exception e) {
                            Log.e("ERR_GET_PHOTOS", e.getMessage());
                            Toast.makeText(ReadingFormActivity.this, "An error occurred while fetching photos", Toast.LENGTH_SHORT).show();
                        }

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

    /**
     * BT PRINTING
     */
    // This will find a bluetooth printer device
    public void findBTDevice() {
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {
                Toast.makeText(this, "No bluetooth adapter available", Toast.LENGTH_SHORT).show();
            } else {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBluetooth = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBluetooth, 0);
                    Toast.makeText(this, "Bluetooth is disabled!", Toast.LENGTH_SHORT).show();
                } else {
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                            .getBondedDevices();
                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            if (device.getName().contains("TM-")) {
                                mmDevice = device;
                                Log.e("PRINTER_LOCATED", "Printer Found");
                                break;
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            Toast.makeText(this, "No bluetooth adapter available", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "No bluetooth adapter available", Toast.LENGTH_SHORT).show();
        }
    }

    public void print(Bills bills, Rates rates, DownloadedPreviousReadings dpr) {
        try {
            findBTDevice();
            if (mmDevice != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            printer = new Printer(Printer.TM_P80, Printer.MODEL_ANK, ReadingFormActivity.this);

                            printer.addFeedLine(1);

                            // HEADER
                            printer.addTextAlign(Printer.ALIGN_CENTER);
                            StringBuilder headerBuilder = new StringBuilder();
                            headerBuilder.append(getResources().getString(R.string.company));
                            headerBuilder.append("\n");
                            headerBuilder.append(getResources().getString(R.string.companyAddress));
                            headerBuilder.append("\n");
                            headerBuilder.append(getResources().getString(R.string.companyTin));
                            headerBuilder.append("\n");
                            headerBuilder.append("STATEMENT OF ACCOUNT");
                            headerBuilder.append("\n");
                            headerBuilder.append(ObjectHelpers.formatShortDateWithDate(bills.getBillingDate()));
                            headerBuilder.append("\n");
                            headerBuilder.append("\n");

                            printer.addText(headerBuilder.toString());
                            printer.addBarcode(bills.getAccountNumber(), Printer.BARCODE_CODE93, Printer.HRI_NONE, Printer.PARAM_UNSPECIFIED, Printer.PARAM_UNSPECIFIED, 80);

                            StringBuilder nameBuilder = new StringBuilder();
                            printer.addTextAlign(Printer.ALIGN_LEFT);
                            printer.addTextSize(1,2);
                            nameBuilder.append("\n");
                            nameBuilder.append("Acct. No.: " + bills.getAccountNumber());
                            nameBuilder.append("\n");
                            nameBuilder.append(dpr.getServiceAccountName());
                            nameBuilder.append("\n");
                            printer.addText(nameBuilder.toString());

                            StringBuilder subheaderBuilder = new StringBuilder();
                            printer.addTextSize(1,1);
                            subheaderBuilder.append(dpr.getPurok() + ", " + dpr.getBarangayFull() + ", " + dpr.getTownFull());
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Billing Month: " + ObjectHelpers.formatShortDate(bills.getServicePeriod()) + "    Mult: " + bills.getMultiplier());
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Meter No:" + (dpr.getMeterSerial() != null ? dpr.getMeterSerial() : '-') + "    Type: " + dpr.getAccountType());
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Prev. Reading: " + ObjectHelpers.formatShortDateWithDate(dpr.getReadingTimestamp()) + "    KWH: " + dpr.getKwhUsed());
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Pres. Reading: " + ObjectHelpers.formatShortDateWithDate(bills.getBillingDate()) + "    KWH: " + bills.getPresentKwh());
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("KwH Used: " + bills.getKwhUsed() + " \tRate: " + ObjectHelpers.roundFour(Double.valueOf(bills.getEffectiveRate())));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Due Date: " + ObjectHelpers.formatShortDateWithDate(bills.getDueDate()));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Meter Reader: " + (user != null ? user.getUsername() : '-'));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Arrears: P " + (dpr.getArrearsTotal() != null ? ObjectHelpers.roundTwo(Double.valueOf(dpr.getArrearsTotal())) : "0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Termed Payments: P " + (dpr.getArrearsLedger() != null ? ObjectHelpers.roundTwo(Double.valueOf(dpr.getArrearsLedger())) : "0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Unpaid Balance: P " + (dpr.getBalance() != null ? ObjectHelpers.roundTwo(Double.valueOf(dpr.getBalance())) : "0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("\n");

                            // RATES
                            subheaderBuilder.append("PARTICULARS \t\tRATE \t  AMOUNT");
                            subheaderBuilder.append("\n");

                            // GENERATION AND TRANSMISSION
                            subheaderBuilder.append("GEN & TRANS REVENUES");
                            subheaderBuilder.append("\n");

                            subheaderBuilder.append("Generation Sys. \t" + (rates.getGenerationSystemCharge() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getGenerationSystemCharge())) : "0.0000")
                                    + "\t  " + (bills.getGenerationSystemCharge() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getGenerationSystemCharge())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Trans. Demand \t\t" + (rates.getTransmissionDeliveryChargeKW() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getTransmissionDeliveryChargeKW())) : "0.0000")
                                    + "\t  " + (bills.getTransmissionDeliveryChargeKW() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getTransmissionDeliveryChargeKW())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Trans. System \t\t" + (rates.getTransmissionDeliveryChargeKWH() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getTransmissionDeliveryChargeKWH())) : "0.0000")
                                    + "\t  " + (bills.getTransmissionDeliveryChargeKWH() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getTransmissionDeliveryChargeKWH())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("System Loss \t\t" + (rates.getSystemLossCharge() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getSystemLossCharge())) : "0.0000")
                                    + "\t  " + (bills.getSystemLossCharge() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getSystemLossCharge())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("OGA \t\t\t" + (rates.getOtherGenerationRateAdjustment() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getOtherGenerationRateAdjustment())) : "0.0000")
                                    + "\t  " + (bills.getOtherGenerationRateAdjustment() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getOtherGenerationRateAdjustment())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("OTCA Demand \t\t" + (rates.getOtherTransmissionCostAdjustmentKW() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getOtherTransmissionCostAdjustmentKW())) : "0.0000")
                                    + "\t  " + (bills.getOtherTransmissionCostAdjustmentKW() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getOtherTransmissionCostAdjustmentKW())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("OTCA System \t\t" + (rates.getOtherTransmissionCostAdjustmentKWH() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getOtherTransmissionCostAdjustmentKWH())) : "0.0000")
                                    + "\t  " + (bills.getOtherTransmissionCostAdjustmentKWH() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getOtherTransmissionCostAdjustmentKWH())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("OSLA \t\t\t" + (rates.getOtherSystemLossCostAdjustment() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getOtherSystemLossCostAdjustment())) : "0.0000")
                                    + "\t  " + (bills.getOtherSystemLossCostAdjustment() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getOtherSystemLossCostAdjustment())) : "0.0"));
                            subheaderBuilder.append("\n");

                            // DSM
                            subheaderBuilder.append("DISTRIBUTION REVENUES");
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Dist. Demand \t\t" + (rates.getDistributionDemandCharge() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getDistributionDemandCharge())) : "0.0000")
                                    + "\t  " + (bills.getDistributionDemandCharge() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getDistributionDemandCharge())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Dist. System \t\t" + (rates.getDistributionSystemCharge() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getDistributionSystemCharge())) : "0.0000")
                                    + "\t  " + (bills.getDistributionSystemCharge() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getDistributionSystemCharge())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Sup. Retail Cust. \t" + (rates.getSupplyRetailCustomerCharge() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getSupplyRetailCustomerCharge())) : "0.0000")
                                    + "\t  " + (bills.getSupplyRetailCustomerCharge() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getSupplyRetailCustomerCharge())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Supply System \t\t" + (rates.getSupplySystemCharge() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getSupplySystemCharge())) : "0.0000")
                                    + "\t  " + (bills.getSupplySystemCharge() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getSupplySystemCharge())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Metering Retail \t" + (rates.getMeteringRetailCustomerCharge() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getMeteringRetailCustomerCharge())) : "0.0000")
                                    + "\t  " + (bills.getMeteringRetailCustomerCharge() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getMeteringRetailCustomerCharge())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Metering System \t" + (rates.getMeteringSystemCharge() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getMeteringSystemCharge())) : "0.0000")
                                    + "\t  " + (bills.getMeteringSystemCharge() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getMeteringSystemCharge())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("RFSC \t\t\t" + (rates.getRFSC() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getRFSC())) : "0.0")
                                    + "\t  " + (bills.getRFSC() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getRFSC())) : "0.0"));
                            subheaderBuilder.append("\n");

                            // UNIVERSAL CHARGES
                            subheaderBuilder.append("UNIVERSAL CHARGES");
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Missionary Elec. \t" + (rates.getMissionaryElectrificationCharge() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getMissionaryElectrificationCharge())) : "0.0000")
                                    + "\t  " + (bills.getMissionaryElectrificationCharge() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getMissionaryElectrificationCharge())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Environmental Chrg. \t" + (rates.getEnvironmentalCharge() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getEnvironmentalCharge())) : "0.0000")
                                    + "\t  " + (bills.getEnvironmentalCharge() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getEnvironmentalCharge())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Stranded Cont. \t\t" + (rates.getStrandedContractCosts() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getStrandedContractCosts())) : "0.0000")
                                    + "\t  " + (bills.getStrandedContractCosts() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getStrandedContractCosts())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("NPC Stranded Debt \t" + (rates.getNPCStrandedDebt() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getNPCStrandedDebt())) : "0.0000")
                                    + "\t  " + (bills.getNPCStrandedDebt() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getNPCStrandedDebt())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("FIT All. \t\t" + (rates.getFeedInTariffAllowance() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getFeedInTariffAllowance())) : "0.0000")
                                    + "\t  " + (bills.getFeedInTariffAllowance() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getFeedInTariffAllowance())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("REDCI \t\t\t" + (rates.getMissionaryElectrificationREDCI() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getMissionaryElectrificationREDCI())) : "0.0000")
                                    + "\t  " + (bills.getMissionaryElectrificationREDCI() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getMissionaryElectrificationREDCI())) : "0.0"));
                            subheaderBuilder.append("\n");

                            // OTHERS
                            subheaderBuilder.append("OTHERS");
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Lifeline Rate\t\t" + (rates.getLifelineRate() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getLifelineRate())) : "0.0000")
                                    + "\t  " + (bills.getLifelineRate() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getLifelineRate())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("ICC Subsidy\t\t" + (rates.getInterClassCrossSubsidyCharge() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getInterClassCrossSubsidyCharge())) : "0.0000")
                                    + "\t  " + (bills.getInterClassCrossSubsidyCharge() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getInterClassCrossSubsidyCharge())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("PPA Refund\t\t" + (rates.getPPARefund() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getPPARefund())) : "0.0000")
                                    + "\t  " + (bills.getPPARefund() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getPPARefund())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Sen. Citizen Sub. \t" + (rates.getSeniorCitizenSubsidy() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getSeniorCitizenSubsidy())) : "0.0000")
                                    + "\t  " + (bills.getSeniorCitizenSubsidy() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getSeniorCitizenSubsidy())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("OLRA \t\t\t" + (rates.getOtherLifelineRateCostAdjustment() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getOtherLifelineRateCostAdjustment())) : "0.0000")
                                    + "\t  " + (bills.getOtherLifelineRateCostAdjustment() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getOtherLifelineRateCostAdjustment())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("SC Disc. & Ajd. \t" + (rates.getSeniorCitizenDiscountAndSubsidyAdjustment() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getSeniorCitizenDiscountAndSubsidyAdjustment())) : "0.0000")
                                    + "\t  " + (bills.getSeniorCitizenDiscountAndSubsidyAdjustment() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getSeniorCitizenDiscountAndSubsidyAdjustment())) : "0.0"));
                            subheaderBuilder.append("\n");

                            // OTHERS
                            subheaderBuilder.append("TAXES");
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Generation VAT \t\t" + (rates.getGenerationVAT() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getGenerationVAT())) : "0.0000")
                                    + "\t  " + (bills.getGenerationVAT() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getGenerationVAT())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Transmission VAT \t" + (rates.getTransmissionVAT() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getTransmissionVAT())) : "0.0000")
                                    + "\t  " + (bills.getTransmissionVAT() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getTransmissionVAT())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Distribution VAT \t" + (rates.getDistributionVAT() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getDistributionVAT())) : "0.0000")
                                    + "\t  " + (bills.getDistributionVAT() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getDistributionVAT())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("System Loss VAT \t" + (rates.getSystemLossVAT() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getSystemLossVAT())) : "0.0000")
                                    + "\t  " + (bills.getSystemLossVAT() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getSystemLossVAT())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Franchise Tax\t\t" + (rates.getFranchiseTax() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getFranchiseTax())) : "0.0000")
                                    + "\t  " + (bills.getFranchiseTax() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getFranchiseTax())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Business Tax\t\t" + (rates.getBusinessTax() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getBusinessTax())) : "0.0000")
                                    + "\t  " + (bills.getBusinessTax() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getBusinessTax())) : "0.0"));
                            subheaderBuilder.append("\n");
                            subheaderBuilder.append("Real Property Tax\t" + (rates.getRealPropertyTax() != null ? ObjectHelpers.roundFour(Double.valueOf(rates.getRealPropertyTax())) : "0.0000")
                                    + "\t  " + (bills.getRealPropertyTax() != null ? ObjectHelpers.roundTwo(Double.valueOf(bills.getRealPropertyTax())) : "0.0"));
                            subheaderBuilder.append("\n");

                            printer.addText(subheaderBuilder.toString());

                            // NET AMOUNT
                            printer.addTextAlign(Printer.ALIGN_CENTER);
                            StringBuilder amountDueLbl = new StringBuilder();
                            amountDueLbl.append("--------------------------------------");
                            amountDueLbl.append("\n");
                            amountDueLbl.append("Amount Due:");
                            amountDueLbl.append("\n");
                            printer.addText(amountDueLbl.toString());

                            StringBuilder amountDue = new StringBuilder();
                            printer.addTextSize(2, 3);
                            amountDue.append("P " + ObjectHelpers.roundTwo(Double.valueOf(bills.getNetAmount())));
                            amountDue.append("\n");
                            printer.addText(amountDue.toString());

                            // NOTES
                            printer.addTextAlign(Printer.ALIGN_LEFT);
                            StringBuilder notes = new StringBuilder();
                            printer.addTextSize(1, 1);
                            notes.append("NOTE: \tPlease pay this bill within nine (9) days upon receipt hereof to avoid disconnection of your electric services.");
                            notes.append("\n");
                            notes.append("Rates are net of refund per ERC order 2012-018CF");
                            notes.append("\n");
                            printer.addText(notes.toString());

                            // ADDITIONAL
                            printer.addTextAlign(Printer.ALIGN_CENTER);
                            StringBuilder additional = new StringBuilder();
                            additional.append("*PLS PRESENT THIS STATEMENT UPON PAYMENT*");
                            additional.append("\n");
                            printer.addText(additional.toString());

                            // STUB
                            StringBuilder stubLine = new StringBuilder();
                            stubLine.append("------------------------------------------");
                            stubLine.append("\n");
                            printer.addText(stubLine.toString());

                            printer.addTextAlign(Printer.ALIGN_LEFT);
                            StringBuilder dateBldr = new StringBuilder();
                            dateBldr.append("Date: " + ObjectHelpers.getCurrentDate() + " " + ObjectHelpers.getCurrentTime());
                            dateBldr.append("\n");
                            dateBldr.append("\n");
                            printer.addText(dateBldr.toString());

                            StringBuilder nameBuilderStub = new StringBuilder();
                            printer.addTextAlign(Printer.ALIGN_LEFT);
                            printer.addTextSize(1,2);
                            nameBuilderStub.append("\n");
                            nameBuilderStub.append("Acct. No.: " + bills.getAccountNumber());
                            nameBuilderStub.append("\n");
                            nameBuilderStub.append(dpr.getServiceAccountName());
                            nameBuilderStub.append("\n");
                            printer.addText(nameBuilderStub.toString());

                            StringBuilder subheaderBuilderStub = new StringBuilder();
                            printer.addTextSize(1,1);
                            subheaderBuilderStub.append(dpr.getPurok() + ", " + dpr.getBarangayFull() + ", " + dpr.getTownFull());
                            subheaderBuilderStub.append("\n");
                            subheaderBuilderStub.append("\n");
                            subheaderBuilderStub.append("Billing Month: " + ObjectHelpers.formatShortDate(bills.getServicePeriod()) + "\tMult.: " + dpr.getMultiplier());
                            subheaderBuilderStub.append("\n");
                            subheaderBuilderStub.append("Meter No:" + (dpr.getMeterSerial() != null ? dpr.getMeterSerial() : '-') + "\tType: " + dpr.getAccountType());
                            subheaderBuilderStub.append("\n");
                            subheaderBuilderStub.append("KwH Used: " + bills.getKwhUsed() + " \tRate: " + ObjectHelpers.roundFour(Double.valueOf(bills.getEffectiveRate())));
                            subheaderBuilderStub.append("\n");
                            subheaderBuilderStub.append("Amount Due: P " + ObjectHelpers.roundTwo(Double.valueOf(bills.getNetAmount())));
                            subheaderBuilderStub.append("\n");
                            subheaderBuilderStub.append("Arrears: P " + ObjectHelpers.roundTwo(Double.valueOf(dpr.getArrearsTotal())));
                            subheaderBuilderStub.append("\n");
                            subheaderBuilderStub.append("\n");
                            printer.addText(subheaderBuilderStub.toString());

                            printer.addCut(Printer.CUT_FEED);

                            printer.connect("BT:" + mmDevice.getAddress(), Printer.PARAM_DEFAULT);
                            printer.beginTransaction();
                            printer.sendData(Printer.PARAM_DEFAULT);
                        } catch (Exception e ) {
                            e.printStackTrace();
                        }

                        printer.setReceiveEventListener(new ReceiveListener() {
                            @Override
                            public void onPtrReceive(Printer printer, int i, PrinterStatusInfo printerStatusInfo, String s) {
                                if (i == Epos2CallbackCode.CODE_SUCCESS){
//            Toast.makeText(this, "Printing...", Toast.LENGTH_SHORT).show();
                                    Log.e("PRINTING_SUCCESS", "Printing ok");

                                    try {
                                        printer.disconnect();
                                        printer.clearCommandBuffer();
                                    } catch (Epos2Exception e) {
                                        try {
                                            printer.disconnect();
                                        } catch (Epos2Exception epos2Exception) {
                                            epos2Exception.printStackTrace();
                                        }
                                        printer.clearCommandBuffer();
                                        e.printStackTrace();
                                    }
                                }else{
                                    try {
                                        printer.disconnect();
                                        printer.clearCommandBuffer();
                                    } catch (Epos2Exception e) {
                                        try {
                                            printer.disconnect();
                                        } catch (Epos2Exception epos2Exception) {
                                            epos2Exception.printStackTrace();
                                        }
                                        printer.clearCommandBuffer();
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                });
            } else {
                AlertHelpers.showMessageDialog(this, "Printing Error", "Check if Bluetooth is enabled. Also check if the printer is connected to the device.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
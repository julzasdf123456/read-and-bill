package com.lopez.julz.readandbill;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.gson.JsonParser;
import com.lopez.julz.readandbill.adapters.AccountsListAdapter;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.DownloadedPreviousReadings;
import com.lopez.julz.readandbill.dao.Tracks;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.LibraryLoader;
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
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolLongClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.CannotAddLayerException;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.CannotAddSourceException;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReadingConsoleActivity extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback, LocationListener {

    public MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private LocationComponent locationComponent;
    public Style style;
    public SymbolManager symbolManager;

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1002;

    public AppDatabase db;

    public List<DownloadedPreviousReadings> downloadedPreviousReadingsList;

    public String userId, areaCode, groupCode, servicePeriod;
    public List<Point> tracksList;

    public String LINE_SOURCE_ID = "tracks";
    public String LINE_LAYER_ID = "tracksLayer";

    /**
     * SearchBar
     */
    public FloatingActionButton backBtnReading, searchBtnReading;
    public AutoCompleteTextView searchAutoComplete;
    public String[] autoCompleteData;

    /**
     * Bottom Nav View
     */
    public BottomNavigationView bottomNavView;

    /**
     * Bottom Sheet List
     */
    public BottomSheetDialog bottomSheetDialog;
    public FloatingActionButton closeBottomSheet;
    public RecyclerView readingListBottomSheetRecyclerview;
    public AccountsListAdapter accountsListAdapter;

    int loadCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LibraryLoader.load();
        Mapbox.getInstance(this, getResources().getString(R.string.mapbox_access_token));
        Window window = getWindow();
        WindowManager.LayoutParams winParams = window.getAttributes();
        winParams.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        window.setAttributes(winParams);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_reading_console);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        userId = getIntent().getExtras().getString("USERID");
        areaCode = getIntent().getExtras().getString("AREACODE");
        groupCode = getIntent().getExtras().getString("GROUPCODE");
        servicePeriod = getIntent().getExtras().getString("SERVICEPERIOD");

        mapView = findViewById(R.id.mapviewReading);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        downloadedPreviousReadingsList = new ArrayList<>();
        tracksList = new ArrayList<>();

        /**
         * Search Bar
         */
        backBtnReading = findViewById(R.id.backBtnReading);
        searchBtnReading = findViewById(R.id.searchBtnReading);
        searchAutoComplete = findViewById(R.id.searchAutoComplete);

        backBtnReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        searchBtnReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    List<DownloadedPreviousReadings> searchRes = downloadedPreviousReadingsList.stream()
                            .filter(downloadedPreviousReadings -> downloadedPreviousReadings.getServiceAccountName().equals(searchAutoComplete.getText().toString()))
                            .collect(Collectors.toList());

                    if (searchRes != null & searchRes.size() > 0) {
                        DownloadedPreviousReadings dprSearch = searchRes.get(0);

                        if (hasLatLong(dprSearch)) {
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(Double.valueOf(dprSearch.getLatitude()), Double.valueOf(dprSearch.getLongitude())))
                                    .zoom(18)
                                    .build();

                            if (mapboxMap != null) {
                                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1200);
                            } else {
                                Toast.makeText(ReadingConsoleActivity.this, "Map is still loading, try again in a couple of seconds", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(ReadingConsoleActivity.this, "This feature is only available on Android N and above!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /**
         * Bottom Sheet
         */
        bottomSheetDialog = new BottomSheetDialog(ReadingConsoleActivity.this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.bottomsheet_reading_list, null);
        bottomSheetDialog.setContentView(view);
        closeBottomSheet = view.findViewById(R.id.closeBottomSheet);
        readingListBottomSheetRecyclerview = view.findViewById(R.id.readingListBottomSheetRecyclerview);
        accountsListAdapter = new AccountsListAdapter(downloadedPreviousReadingsList, ReadingConsoleActivity.this, servicePeriod, userId);
        readingListBottomSheetRecyclerview.setAdapter(accountsListAdapter);
        readingListBottomSheetRecyclerview.setLayoutManager(new LinearLayoutManager(ReadingConsoleActivity.this));

        /**
         * Bottom Nav View
         */
        bottomNavView = findViewById(R.id.bottomNavView);
        bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.drivingMode:

                        break;
                    case R.id.listView:

                        bottomSheetDialog.show();
                        break;
                }
                return false;
            }
        });

        closeBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

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

                    new GetReadingListTracks().execute();

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

        if (loadCount > 0) {
            new GetReadingListTracks().execute();
        }

        loadCount++;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
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

    /**
     * PLOT TRACKS
     */
    public class GetReadingListTracks extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (symbolManager != null) {
                symbolManager.deleteAll();
            }
            symbolManager = new SymbolManager(mapView, mapboxMap, style);

            symbolManager.setIconAllowOverlap(true);
            symbolManager.setTextAllowOverlap(true);
            downloadedPreviousReadingsList.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                downloadedPreviousReadingsList.addAll(db.downloadedPreviousReadingsDao().getAllFromSchedule(servicePeriod, areaCode, groupCode));

                int max = downloadedPreviousReadingsList.size();
                autoCompleteData = new String[max];
                for (int i=0; i<max; i++) {
                    autoCompleteData[i] = downloadedPreviousReadingsList.get(i).getServiceAccountName();
                    publishProgress(i);
//                    Thread.sleep(10);
                }
            } catch (Exception e) {
                Log.e("ERR_GET_LIST", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            DownloadedPreviousReadings dpr = downloadedPreviousReadingsList.get(values[0]);
            if (hasLatLong(dpr)) {
                SymbolOptions symbolOptions;
                if (dpr.getAccountStatus().equals("ACTIVE")) {
                    if (dpr.getStatus() != null) {
                        if (dpr.getStatus().equals("READ")) {
                            symbolOptions = new SymbolOptions()
                                    .withLatLng(new LatLng(Double.valueOf(dpr.getLatitude()), Double.valueOf(dpr.getLongitude())))
                                    .withData(new JsonParser().parse("{" +
                                            "'id' : '" + dpr.getId() + "'," +
                                            "'svcPeriod' : '" + dpr.getServicePeriod() + "'}"))
                                    .withIconImage("marker-blue")
                                    .withIconSize(.30f);
                        } else {
                            symbolOptions = new SymbolOptions()
                                    .withLatLng(new LatLng(Double.valueOf(dpr.getLatitude()), Double.valueOf(dpr.getLongitude())))
                                    .withData(new JsonParser().parse("{" +
                                            "'id' : '" + dpr.getId() + "'," +
                                            "'svcPeriod' : '" + dpr.getServicePeriod() + "'}"))
                                    .withIconImage("place-black-24dp")
                                    .withIconSize(1f);
                        }
                    } else {
                        symbolOptions = new SymbolOptions()
                                .withLatLng(new LatLng(Double.valueOf(dpr.getLatitude()), Double.valueOf(dpr.getLongitude())))
                                .withData(new JsonParser().parse("{" +
                                        "'id' : '" + dpr.getId() + "'," +
                                        "'svcPeriod' : '" + dpr.getServicePeriod() + "'}"))
                                .withIconImage("place-black-24dp")
                                .withIconSize(1f);
                    }

                } else if (dpr.getAccountStatus().equals("DISCONNECTED")) {
                    symbolOptions = new SymbolOptions()
                            .withLatLng(new LatLng(Double.valueOf(dpr.getLatitude()), Double.valueOf(dpr.getLongitude())))
                            .withData(new JsonParser().parse("{" +
                                    "'id' : '" + dpr.getId() + "'," +
                                    "'svcPeriod' : '" + dpr.getServicePeriod() + "'}"))
                            .withIconImage("level-crossing")
                            .withIconSize(.50f);
                } else {
                    symbolOptions = new SymbolOptions()
                            .withLatLng(new LatLng(Double.valueOf(dpr.getLatitude()), Double.valueOf(dpr.getLongitude())))
                            .withData(new JsonParser().parse("{" +
                                    "'id' : '" + dpr.getId() + "'," +
                                    "'svcPeriod' : '" + dpr.getServicePeriod() + "'}"))
                            .withIconImage("marker-blue")
                            .withIconSize(.30f);
                }

                Symbol symbol = symbolManager.create(symbolOptions);

            }
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ReadingConsoleActivity.this, android.R.layout.simple_list_item_1, autoCompleteData);
            searchAutoComplete.setAdapter(adapter);

            accountsListAdapter.notifyDataSetChanged();

            if (symbolManager != null) {
                symbolManager.addClickListener(new OnSymbolClickListener() {
                    @Override
                    public void onAnnotationClick(Symbol symbol) {
                        //                    Toast.makeText(HomeActivity.this, symbol.getData().getAsJsonObject().get("scId").getAsString(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ReadingConsoleActivity.this, ReadingFormActivity.class);
                        intent.putExtra("ID", symbol.getData().getAsJsonObject().get("id").getAsString());
                        intent.putExtra("SERVICEPERIOD", servicePeriod);
                        intent.putExtra("USERID", userId);
                        startActivity(intent);
                    }
                });
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
}
package com.lopez.julz.readandbill;

import static android.view.View.GONE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonParser;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.TrackNames;
import com.lopez.julz.readandbill.dao.Tracks;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
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
import com.mapbox.mapboxsdk.style.layers.CannotAddLayerException;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.CannotAddSourceException;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RecordTracksActivity extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback, LocationListener {

    public MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private LocationComponent locationComponent;
    public Style style;
    public SymbolManager symbolManager;

    public List<Point> tracksList;

    public FloatingActionButton startTrackingBtn, markMeterBtn, backBtn, drivingModeBtn;

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1002;

    Timer timer;
    TimerTask timerTask;

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    public boolean isTracking = false, isDriving = false;

    public String trackName, trackNameId;
    public TextView trackNameLayout;

    public String LINE_SOURCE_ID = "tracks";
    public String LINE_LAYER_ID = "tracksLayer";

    public AppDatabase db;

    public LatLng prevLatLong, currentLatLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_record_tracks);

        trackName = getIntent().getExtras().getString("TRACK_NAME");

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        mapView = findViewById(R.id.mapViewTracking);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        tracksList = new ArrayList<>();

        startTrackingBtn = findViewById(R.id.startTrackingBtn);
        startTrackingBtn.setEnabled(false);
        markMeterBtn = findViewById(R.id.markMeterBtn);
        backBtn = findViewById(R.id.backBtn);
        trackNameLayout = findViewById(R.id.trackName);
        drivingModeBtn = findViewById(R.id.drivingModeBtn);

        trackNameLayout.setText(trackName);

        startTrackingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTracking) {
                    drivingModeBtn.setEnabled(false);
                    startRecordingTracks();
                    isTracking = true;
                    startTrackingBtn.setImageResource(R.drawable.ic_baseline_stop_24);
                    startTrackingBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                    Toast.makeText(RecordTracksActivity.this, "Track recording started", Toast.LENGTH_SHORT).show();
                } else {
                    drivingModeBtn.setEnabled(true);
                    stopRecordingTracks();
                    isTracking = false;
                    startTrackingBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    startTrackingBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.teal_200)));
                    Toast.makeText(RecordTracksActivity.this, "Track recording stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        drivingModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isDriving) {
                    startTrackingBtn.setEnabled(false);
                    startDriving();
                    drivingModeBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                    isDriving = true;
                    Toast.makeText(RecordTracksActivity.this, "Driving mode started", Toast.LENGTH_SHORT).show();
                } else {
                    startTrackingBtn.setEnabled(true);
                    stopDriving();
                    drivingModeBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.teal_200)));
                    isDriving = false;
                    Toast.makeText(RecordTracksActivity.this, "Driving mode stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

                    new GetTrackNameId().execute(trackName);

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
    protected void onResume() {
        super.onResume();
        mapView.onResume();
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

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Snackbar.make(startTrackingBtn, locationComponent.getLastKnownLocation().getLatitude() + " - " + locationComponent.getLastKnownLocation().getLongitude(), Snackbar.LENGTH_SHORT).show();
    }

    public void startRecordingTracks() {
        try {
            timer = new Timer();

            timerTask = new TimerTask() {
                public void run() {

                    //use a handler to run a toast that shows the current timestamp
                    handler.post(new Runnable() {
                        public void run() {
                            if (locationComponent != null) {
                                if (locationComponent.getLastKnownLocation() != null) {
                                    if (style != null) {
                                        currentLatLong = new LatLng(locationComponent.getLastKnownLocation().getLatitude(), locationComponent.getLastKnownLocation().getLongitude());

                                        CameraPosition position = new CameraPosition.Builder()
                                                .target(currentLatLong)
                                                .tilt(0)
                                                .padding(0, 0, 0, 0)
                                                .build();
                                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 300);

                                        if (prevLatLong != null) {
                                            // Calculate previous distance from current
                                            double distanceFromPrev = TurfMeasurement.distance(Point.fromLngLat(prevLatLong.getLongitude(), prevLatLong.getLatitude()),
                                                    Point.fromLngLat(currentLatLong.getLongitude(), currentLatLong.getLatitude()),
                                                    TurfConstants.UNIT_METERS);

                                            // only save if distance from prev is greater than 2 meters
                                            if (distanceFromPrev > 2.0) {
                                                new SaveTracks().execute(currentLatLong.getLatitude()+"", currentLatLong.getLongitude()+"");
                                                new GetTracks().execute(trackNameId);
                                            }
                                        } else {
                                            new SaveTracks().execute(locationComponent.getLastKnownLocation().getLatitude()+"", locationComponent.getLastKnownLocation().getLongitude()+"");
                                            new GetTracks().execute(trackNameId);
                                        }

                                        prevLatLong = currentLatLong;
                                    }
                                }
                            }
                        }
                    });
                }
            };

            //schedule the timer, after the first 5000ms the TimerTask will run every 1.5 secs
            timer.schedule(timerTask, 50, 1500); //
        } catch (Exception e) {
            Log.e("ERR_REC_TRCKS", e.getMessage());
        }
    }

    public void stopRecordingTracks() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        } catch (Exception e) {
            Log.e("ERR_STOPING_REC_TRCKS", e.getMessage());
        }
    }

    public class GetTrackNameId extends AsyncTask<String, Void, Void> {

        TrackNames trackName;

        @Override
        protected Void doInBackground(String... strings) {
            try {
                trackName = db.trackNamesDao().getOneByTrackName(strings[0]);
                trackNameId = trackName.getId() + "";
            } catch (Exception e) {
                Log.e("ERR_GET_TRCK_ID", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (trackName.getUploaded().equals("DL")) {
                startTrackingBtn.setVisibility(GONE);
            } else {
                startTrackingBtn.setEnabled(true);
            }

            new GetTracks().execute(trackNameId);
        }
    }

    public class SaveTracks extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                db.tracksDao().insertAll(new Tracks(trackNameId, strings[0], strings[1], "No", ObjectHelpers.getCurrentTimestamp())); // strings[0] = latitude, strings[1] = longitude
            } catch (Exception e) {
                Log.e("ERR_SV_TRCKS", e.getMessage());
            }
            return null;
        }
    }

    public class GetTracks extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tracksList.clear();
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                List<Tracks> tracksListX = db.tracksDao().getAllByTrackNameId(trackNameId);

                for (Tracks tracks : tracksListX) {
                    tracksList.add(Point.fromLngLat(Double.valueOf(tracks.getLongitude()), Double.valueOf(tracks.getLatitude())));
                }
            } catch (Exception e) {
                Log.e("ERR_GET_TRCKS", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            try {
                if (tracksList.size() > 0) {
                    style.removeSource(LINE_SOURCE_ID);
                    style.removeLayer(LINE_LAYER_ID);
                    // ADD LINE
                    if (style != null) {
                        style.addSource(new GeoJsonSource(LINE_SOURCE_ID,
                                FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(
                                        LineString.fromLngLats(tracksList)
                                )})));

                        style.addLayer(new LineLayer(LINE_LAYER_ID, LINE_SOURCE_ID).withProperties(
                                lineCap(Property.LINE_CAP_BUTT),
                                lineJoin(Property.LINE_JOIN_ROUND),
                                lineWidth(7f),
                                lineColor(Color.parseColor("#e53935"))
                        ));
                    }
                }
            } catch (CannotAddSourceException ex) {
                String oldSourceId = LINE_SOURCE_ID;
                String oldLayerId = LINE_LAYER_ID;

                LINE_SOURCE_ID = ObjectHelpers.generateRandomString() + "_SOURCEID";
                LINE_LAYER_ID = ObjectHelpers.generateRandomString() + "_LAYERID";

                if (style != null) {
                    style.addSource(new GeoJsonSource(LINE_SOURCE_ID,
                            FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(
                                    LineString.fromLngLats(tracksList)
                            )})));

                    style.addLayer(new LineLayer(LINE_LAYER_ID, LINE_SOURCE_ID).withProperties(
                            lineCap(Property.LINE_CAP_BUTT),
                            lineJoin(Property.LINE_JOIN_ROUND),
                            lineWidth(7f),
                            lineColor(Color.parseColor("#e53935"))
                    ));
                }

                style.removeSource(oldSourceId);
                style.removeLayer(oldLayerId);
            } catch (CannotAddLayerException exp) {
                String oldSourceId = LINE_SOURCE_ID;
                String oldLayerId = LINE_LAYER_ID;

                LINE_SOURCE_ID = ObjectHelpers.generateRandomString() + "_SOURCEID";
                LINE_LAYER_ID = ObjectHelpers.generateRandomString() + "_LAYERID";

                if (style != null) {
                    style.addSource(new GeoJsonSource(LINE_SOURCE_ID,
                            FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(
                                    LineString.fromLngLats(tracksList)
                            )})));

                    style.addLayer(new LineLayer(LINE_LAYER_ID, LINE_SOURCE_ID).withProperties(
                            lineCap(Property.LINE_CAP_BUTT),
                            lineJoin(Property.LINE_JOIN_ROUND),
                            lineWidth(7f),
                            lineColor(Color.parseColor("#e53935"))
                    ));
                }

                style.removeSource(oldSourceId);
                style.removeLayer(oldLayerId);
            } catch (Exception e) {
                String oldSourceId = LINE_SOURCE_ID;
                String oldLayerId = LINE_LAYER_ID;

                LINE_SOURCE_ID = ObjectHelpers.generateRandomString() + "_SOURCEID";
                LINE_LAYER_ID = ObjectHelpers.generateRandomString() + "_LAYERID";

                if (style != null) {
                    style.addSource(new GeoJsonSource(LINE_SOURCE_ID,
                            FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(
                                    LineString.fromLngLats(tracksList)
                            )})));

                    style.addLayer(new LineLayer(LINE_LAYER_ID, LINE_SOURCE_ID).withProperties(
                            lineCap(Property.LINE_CAP_BUTT),
                            lineJoin(Property.LINE_JOIN_ROUND),
                            lineWidth(7f),
                            lineColor(Color.parseColor("#e53935"))
                    ));
                }
                style.removeSource(oldSourceId);
                style.removeLayer(oldLayerId);
            }
        }
    }

    public void addTrackMarker(Style style, LatLng latLng) {
        try {
            symbolManager = new SymbolManager(mapView, mapboxMap, style);

            symbolManager.setIconAllowOverlap(true);
            symbolManager.setTextAllowOverlap(true);

            SymbolOptions symbolOptions = new SymbolOptions()
                    .withLatLng(latLng)
                    .withIconImage("tw-provincial-2")
                    .withIconSize(.20f);

            Symbol symbol = symbolManager.create(symbolOptions);
        } catch (Exception e) {
            Log.e("ERR_ADD_MRKER", e.getMessage());
        }
    }

    public void startDriving() {
        try {
            timer = new Timer();

            timerTask = new TimerTask() {
                public void run() {

                    //use a handler to run a toast that shows the current timestamp
                    handler.post(new Runnable() {
                        public void run() {
                            if (locationComponent != null) {
                                if (locationComponent.getLastKnownLocation() != null) {
                                    if (style != null) {
                                        CameraPosition position = new CameraPosition.Builder()
                                                .target(new LatLng(locationComponent.getLastKnownLocation().getLatitude(), locationComponent.getLastKnownLocation().getLongitude()))
                                                .tilt(0)
                                                .padding(0, 0, 0, 0)
                                                .build();
                                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 300);
                                    }
                                }
                            }
                        }
                    });
                }
            };

            //schedule the timer, after the first 5000ms the TimerTask will run every 1.5 secs
            timer.schedule(timerTask, 50, 1500); //
        } catch (Exception e) {
            Log.e("ERR_START_DRIVING", e.getMessage());
        }
    }

    public void stopDriving() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }

            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(locationComponent.getLastKnownLocation().getLatitude(), locationComponent.getLastKnownLocation().getLongitude()))
                    .zoom(15)
                    .tilt(0)
                    .padding(0, 0, 0, 0)
                    .build();
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 300);
        } catch (Exception e) {
            Log.e("ERR_STOPING_REC_TRCKS", e.getMessage());
        }
    }
}
package com.lopez.julz.readandbill;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lopez.julz.readandbill.api.RequestPlaceHolder;
import com.lopez.julz.readandbill.api.RetrofitBuilder;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.TrackNames;
import com.lopez.julz.readandbill.dao.Tracks;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;
import com.lopez.julz.readandbill.helpers.TracksTmp;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
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
import com.mapbox.mapboxsdk.style.layers.CannotAddLayerException;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.CannotAddSourceException;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadActivity extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback {

    public TextView trackName;
    public FloatingActionButton backBtn, downloadBtn;

    public String trackNameText, trackNameId;

    public MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private LocationComponent locationComponent;
    public Style style;
    public SymbolManager symbolManager;

    public RetrofitBuilder retrofitBuilder;
    private RequestPlaceHolder requestPlaceHolder;

    public List<Tracks> tracksList;
    public List<Point> tracksListLine;

    public String LINE_SOURCE_ID = "tracks";
    public String LINE_LAYER_ID = "tracksLayer";

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1002;

    public AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_download);

        trackNameText = getIntent().getExtras().getString("TRACK_NAME");
        trackNameId = getIntent().getExtras().getString("TRACK_ID");

        retrofitBuilder = new RetrofitBuilder();
        requestPlaceHolder = retrofitBuilder.getRetrofit().create(RequestPlaceHolder.class);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        mapView = findViewById(R.id.mapViewDownload);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        trackName = findViewById(R.id.trackName);
        backBtn = findViewById(R.id.backBtn);
        downloadBtn = findViewById(R.id.downloadBtn);
        tracksList = new ArrayList<>();
        tracksListLine = new ArrayList<>();

        trackName.setText(trackNameText);
        downloadBtn.setEnabled(false);

        fetchTracks();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SaveTrackName().execute();
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
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public void fetchTracks() {
        try {
            Call<List<TracksTmp>> tracksCall = requestPlaceHolder.getDownloadableTracks(trackNameId);

            tracksCall.enqueue(new Callback<List<TracksTmp>>() {
                @Override
                public void onResponse(Call<List<TracksTmp>> call, Response<List<TracksTmp>> response) {
                    if (response.isSuccessful()) {
                        List<TracksTmp> tracksTmpList = response.body();
                        for(TracksTmp tracksTmp : tracksTmpList) {
                            tracksList.add(new Tracks(tracksTmp.getTrackNameId(), tracksTmp.getLatitude(), tracksTmp.getLongitude(), "DL", tracksTmp.getCaptured()));
                            tracksListLine.add(Point.fromLngLat(Double.valueOf(tracksTmp.getLongitude()), Double.valueOf(tracksTmp.getLatitude())));
                        }

                        downloadBtn.setEnabled(true);

                        // DRAW MAP
                        try {
                            if (tracksListLine.size() > 0) {
                                style.removeSource(LINE_SOURCE_ID);
                                style.removeLayer(LINE_LAYER_ID);
                                // ADD LINE
                                if (style != null) {
                                    style.addSource(new GeoJsonSource(LINE_SOURCE_ID,
                                            FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(
                                                    LineString.fromLngLats(tracksListLine)
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
                                                LineString.fromLngLats(tracksListLine)
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
                                                LineString.fromLngLats(tracksListLine)
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
                                                LineString.fromLngLats(tracksListLine)
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
                    } else {
                        Log.e("ERR_FETCHING_TRCKS", response.message());
                    }
                }

                @Override
                public void onFailure(Call<List<TracksTmp>> call, Throwable t) {
                    Log.e("ERR_FETCHING_TRCKS", t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("ERR_FETCHING_TRCKS", e.getMessage());
        }
    }

    public class SaveTrackName extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                db.trackNamesDao().insertAll(new TrackNames(trackNameId, trackNameText, "DL"));
            } catch (Exception e) {
                Log.e("ERR_SV_TRCK_NAME", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            new SaveTracks().execute(tracksList);
        }
    }

    public class SaveTracks extends AsyncTask<List<Tracks>, Void, Void> {

        @Override
        protected Void doInBackground(List<Tracks>... lists) {
            if (lists != null) {
                List<Tracks> listTrack = lists[0];

                for(Tracks track : listTrack) {
                    db.tracksDao().insertAll(track);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Toast.makeText(DownloadActivity.this, "Tracks downloaded!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
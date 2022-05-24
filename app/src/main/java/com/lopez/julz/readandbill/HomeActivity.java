package com.lopez.julz.readandbill;

import static com.lopez.julz.readandbill.helpers.ObjectHelpers.hasPermissions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lopez.julz.readandbill.adapters.DownloadReadingListAdapter;
import com.lopez.julz.readandbill.adapters.HomeMenuAdapter;
import com.lopez.julz.readandbill.api.RequestPlaceHolder;
import com.lopez.julz.readandbill.api.RetrofitBuilder;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.Settings;
import com.lopez.julz.readandbill.dao.Users;
import com.lopez.julz.readandbill.dao.UsersDao;
import com.lopez.julz.readandbill.helpers.AlertHelpers;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;
import com.lopez.julz.readandbill.objects.HomeMenu;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView menu_recyclerview;
    public List<HomeMenu> homeMenuList;
    public HomeMenuAdapter homeMenuAdapter;

    public String userId;

    public FloatingActionButton settingsBtn, logout;
    public TextView bottomBarNotif;

    public AppDatabase db;
    public Settings settings;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        WindowManager.LayoutParams winParams = window.getAttributes();
        winParams.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        window.setAttributes(winParams);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_home);

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        userId = getIntent().getExtras().getString("USERID");

        menu_recyclerview = findViewById(R.id.menu_recyclerview);
        homeMenuList = new ArrayList<>();
        homeMenuAdapter = new HomeMenuAdapter(homeMenuList, this, userId);
        menu_recyclerview.setAdapter(homeMenuAdapter);
        menu_recyclerview.setLayoutManager(new GridLayoutManager(this, 2));
        settingsBtn = findViewById(R.id.settingsBtn);
        bottomBarNotif = findViewById(R.id.bottomBarNotif);
        logout = findViewById(R.id.logout);

        addMenu();

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Logout().execute();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new FetchSettings().execute();
    }

    public void addMenu() {
        try {
            homeMenuList.add(new HomeMenu(getDrawable(R.drawable.ic_baseline_download_for_offline_18), "Download", "#4caf50"));
            homeMenuList.add(new HomeMenu(getDrawable(R.drawable.ic_baseline_cloud_upload_24), "Upload", "#ff7043"));
            homeMenuList.add(new HomeMenu(getDrawable(R.drawable.ic_baseline_data_thresholding_24), "Reading List", "#5c6bc0"));
            homeMenuList.add(new HomeMenu(getDrawable(R.drawable.ic_baseline_edit_location_alt_24), "Reading Tracks", "#78909c"));
//            homeMenuList.add(new HomeMenu(getDrawable(R.drawable.ic_baseline_domain_disabled_24), "Disconnection", "#f44336"));

            homeMenuAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("ERR_ADD_MENU", e.getMessage());
        }
    }

    public class FetchSettings extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                settings = db.settingsDao().getSettings();
            } catch (Exception e) {
                Log.e("ERR_FETCH_SETTINGS", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (settings != null) {
                if (settings.getDefaultServer() != null && settings.getDefaultServer().equals("203.177.135.179:8443")) {
                    bottomBarNotif.setText("Server: " + settings.getDefaultServer() + " (via internet)");
                    bottomBarNotif.setBackgroundResource(R.color.teal_700);
                } else {
                    bottomBarNotif.setText("Server: " + settings.getDefaultServer() + " (via local network)");
                    bottomBarNotif.setBackgroundResource(R.color.grey_100);
                }

            } else {
                AlertHelpers.showMessageDialog(HomeActivity.this, "Settings Not Initialized", "Failed to load settings. Go to settings and set all necessary parameters to continue.");
            }
        }
    }

    public class Logout extends AsyncTask<Void, Void, Void> {

        boolean isSuccessful = false;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                UsersDao usersDao = db.usersDao();
                Users user = usersDao.getOneById(userId);
                if (user != null) {
                    user.setLoggedIn("NULL");
                    usersDao.updateAll(user);
                    isSuccessful = true;
                } else {
                    isSuccessful = false;
                }
            } catch (Exception e) {
                Log.e("ERR_LGOUT", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (isSuccessful) {
                finish();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            }
        }
    }
}
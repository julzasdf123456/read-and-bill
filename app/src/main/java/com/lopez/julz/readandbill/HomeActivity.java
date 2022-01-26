package com.lopez.julz.readandbill;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.lopez.julz.readandbill.adapters.HomeMenuAdapter;
import com.lopez.julz.readandbill.objects.HomeMenu;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView menu_recyclerview;
    public List<HomeMenu> homeMenuList;
    public HomeMenuAdapter homeMenuAdapter;

    public String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        WindowManager.LayoutParams winParams = window.getAttributes();
        winParams.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        window.setAttributes(winParams);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_home);

        userId = getIntent().getExtras().getString("USERID");

        menu_recyclerview = findViewById(R.id.menu_recyclerview);
        homeMenuList = new ArrayList<>();
        homeMenuAdapter = new HomeMenuAdapter(homeMenuList, this, userId);
        menu_recyclerview.setAdapter(homeMenuAdapter);
        menu_recyclerview.setLayoutManager(new GridLayoutManager(this, 2));

        addMenu();
    }

    public void addMenu() {
        try {
            homeMenuList.add(new HomeMenu(getDrawable(R.drawable.ic_baseline_download_for_offline_18), "Download", "#4caf50"));
            homeMenuList.add(new HomeMenu(getDrawable(R.drawable.ic_baseline_cloud_upload_24), "Upload", "#ff7043"));
            homeMenuList.add(new HomeMenu(getDrawable(R.drawable.ic_baseline_data_thresholding_24), "Reading List", "#5c6bc0"));
            homeMenuList.add(new HomeMenu(getDrawable(R.drawable.ic_baseline_edit_location_alt_24), "Reading Tracks", "#78909c"));

            homeMenuAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("ERR_ADD_MENU", e.getMessage());
        }
    }
}
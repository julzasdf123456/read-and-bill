package com.lopez.julz.readandbill.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.lopez.julz.readandbill.CreateTracksActivity;
import com.lopez.julz.readandbill.DisconnectionHomeActivity;
import com.lopez.julz.readandbill.DisconnectionMenuListActivity;
import com.lopez.julz.readandbill.DownloadDisconnectionListActivity;
import com.lopez.julz.readandbill.DownloadReadingListActivity;
import com.lopez.julz.readandbill.R;
import com.lopez.julz.readandbill.ReadingListActivity;
import com.lopez.julz.readandbill.UploadDisconnectionActivity;
import com.lopez.julz.readandbill.UploadReadingsActivity;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;
import com.lopez.julz.readandbill.objects.HomeMenu;

import java.util.List;

public class DisconnectionMenuAdapter  extends RecyclerView.Adapter<DisconnectionMenuAdapter.ViewHolder>{

    public List<HomeMenu> homeMenuList;
    public Context context;
    public String userId;

    public DisconnectionMenuAdapter(List<HomeMenu> homeMenuList, Context context, String userId) {
        this.homeMenuList = homeMenuList;
        this.context = context;
        this.userId = userId;
    }

    @NonNull
    @Override
    public DisconnectionMenuAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.recyclerview_layout_menu, parent, false);

        return new DisconnectionMenuAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DisconnectionMenuAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        HomeMenu homeMenu = homeMenuList.get(position);

        holder.title.setText(homeMenu.getTitle());
        holder.imageView.setImageDrawable(homeMenu.getImage());
        holder.parent.setCardBackgroundColor(Color.parseColor(homeMenu.getColor()));
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position == 0) { // download
                    showDownloadOption();
                } else if (position == 1) { // upload
                    context.startActivity(new Intent(context, UploadDisconnectionActivity.class));
                } else if (position == 2) { // disconnection list
                    Intent intent = new Intent(context, DisconnectionMenuListActivity.class);
                    intent.putExtra("USERID", userId);
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return homeMenuList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView title;
        public MaterialCardView parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.menuTitle);
            parent = itemView.findViewById(R.id.parent);
        }
    }

    public void showDownloadOption() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setPadding(10, 5, 10, 5);

            TextView periodLabel = new TextView(context);
            periodLabel.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            periodLabel.setText("Select Period");

            Spinner periodSpinner = new Spinner(context);
            periodSpinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView townLabel = new TextView(context);
            townLabel.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            townLabel.setText("Select Area Code");

            Spinner areaSpinner = new Spinner(context);
            areaSpinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            periodSpinner.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, ObjectHelpers.getPreviousMonths(4)));

            areaSpinner.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09"}));

            linearLayout.addView(periodLabel);
            linearLayout.addView(periodSpinner);
            linearLayout.addView(townLabel);
            linearLayout.addView(areaSpinner);

            builder.setView(linearLayout);

            builder.setTitle("Set Download Params")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(context, DownloadDisconnectionListActivity.class);
                            intent.putExtra("PERIOD", periodSpinner.getSelectedItem().toString());
                            intent.putExtra("AREA", areaSpinner.getSelectedItem().toString());
                            context.startActivity(intent);
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            Log.e("ERR_SHW_DL_OP", e.getMessage());
        }
    }
}

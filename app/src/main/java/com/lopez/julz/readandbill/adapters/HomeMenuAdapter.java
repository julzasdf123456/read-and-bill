package com.lopez.julz.readandbill.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.lopez.julz.readandbill.CreateTracksActivity;
import com.lopez.julz.readandbill.DownloadReadingListActivity;
import com.lopez.julz.readandbill.R;
import com.lopez.julz.readandbill.ReadingListActivity;
import com.lopez.julz.readandbill.objects.HomeMenu;

import java.util.List;

public class HomeMenuAdapter extends RecyclerView.Adapter<HomeMenuAdapter.ViewHolder> {

    public List<HomeMenu> homeMenuList;
    public Context context;
    public String userId;

    public HomeMenuAdapter(List<HomeMenu> homeMenuList, Context context, String userId) {
        this.homeMenuList = homeMenuList;
        this.context = context;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.recyclerview_layout_menu, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        HomeMenu homeMenu = homeMenuList.get(position);

        holder.title.setText(homeMenu.getTitle());
        holder.imageView.setImageDrawable(homeMenu.getImage());
        holder.parent.setCardBackgroundColor(Color.parseColor(homeMenu.getColor()));
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position == 0) { // download
                    Intent intent = new Intent(context, DownloadReadingListActivity.class);
                    intent.putExtra("USERID", userId);
                    context.startActivity(intent);
                } else if (position == 1) { // upload

                } else if (position == 2) { // reading list
                    Intent intent = new Intent(context, ReadingListActivity.class);
                    intent.putExtra("USERID", userId);
                    context.startActivity(intent);
                } else if (position == 3) { // reading tracks
                    context.startActivity(new Intent(context, CreateTracksActivity.class));
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
}

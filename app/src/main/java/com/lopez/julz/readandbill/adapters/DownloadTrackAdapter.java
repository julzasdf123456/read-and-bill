package com.lopez.julz.readandbill.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.lopez.julz.readandbill.DownloadActivity;
import com.lopez.julz.readandbill.R;
import com.lopez.julz.readandbill.RecordTracksActivity;
import com.lopez.julz.readandbill.dao.TrackNames;

import java.util.List;

public class DownloadTrackAdapter extends RecyclerView.Adapter<DownloadTrackAdapter.ViewHolder> {

    public List<TrackNames> trackNamesList;
    public Context context;

    public DownloadTrackAdapter(List<TrackNames> trackNamesList, Context context) {
        this.trackNamesList = trackNamesList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_layout_tracknames, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrackNames trackNames = trackNamesList.get(position);

        holder.trackNameRecyclerView.setText(trackNames.getTrackName());
        holder.parentTrackName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DownloadActivity.class);
                intent.putExtra("TRACK_NAME", trackNames.getTrackName());
                intent.putExtra("TRACK_ID", trackNames.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trackNamesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView trackNameRecyclerView;
        CoordinatorLayout parentTrackName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            trackNameRecyclerView = itemView.findViewById(R.id.trackNameRecyclerView);
            parentTrackName = itemView.findViewById(R.id.parentTrackName);
        }
    }
}

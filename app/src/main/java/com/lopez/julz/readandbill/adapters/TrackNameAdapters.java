package com.lopez.julz.readandbill.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.lopez.julz.readandbill.RecordTracksActivity;
import com.lopez.julz.readandbill.R;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.TrackNames;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;

import java.util.List;

public class TrackNameAdapters extends RecyclerView.Adapter<TrackNameAdapters.ViewHolder> {

    private List<TrackNames> trackNamesList;
    private Context context;

    AppDatabase db;

    public TrackNameAdapters(List<TrackNames> trackNamesList, Context context) {
        this.trackNamesList = trackNamesList;
        this.context = context;
        db = Room.databaseBuilder(context, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();
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
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        TrackNames trackNames = trackNamesList.get(position);

        holder.trackNameRecyclerView.setText(trackNames.getTrackName());

        if (trackNames.getUploaded().equals("No")) {
            holder.trackNameRecyclerView.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.ic_baseline_error_18), null);
        } else if (trackNames.getUploaded().equals("Yes")) {
            holder.trackNameRecyclerView.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.ic_baseline_check_circle_18), null);
        } else if (trackNames.getUploaded().equals("DL")) {
            holder.trackNameRecyclerView.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.ic_baseline_download_for_offline_18), null);
        }
        holder.trackNameRecyclerView.setCompoundDrawablePadding(15);
        holder.parentTrackName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RecordTracksActivity.class);
                intent.putExtra("TRACK_NAME", trackNames.getTrackName());
                context.startActivity(intent);
            }
        });

        holder.parentTrackName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PopupMenu popup = new PopupMenu(context, holder.parentTrackName);
                //inflating menu from xml resource
                popup.inflate(R.menu.tracknames_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                trackNamesList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemChanged(position);
                                notifyDataSetChanged();
                                new DeleteTracks().execute(trackNames.getId());
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

    public class DeleteTracks extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                db.trackNamesDao().deleteOne(strings[0]);
                db.tracksDao().deleteByTrackNameId(strings[0]);
            } catch (Exception e) {
                Log.e("ERR_DELETE", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
    }
}

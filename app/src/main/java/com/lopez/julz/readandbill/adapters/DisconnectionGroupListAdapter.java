package com.lopez.julz.readandbill.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.lopez.julz.readandbill.DisconnectionListActivity;
import com.lopez.julz.readandbill.R;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;
import com.lopez.julz.readandbill.objects.DisconnectionGroupList;

import java.util.List;

public class DisconnectionGroupListAdapter extends RecyclerView.Adapter<DisconnectionGroupListAdapter.ViewHolder> {

    public List<DisconnectionGroupList> disconnectionGroupLists;
    public Context context;
    public String userId;

    public DisconnectionGroupListAdapter(List<DisconnectionGroupList> disconnectionGroupLists, Context context, String userId) {
        this.disconnectionGroupLists = disconnectionGroupLists;
        this.context = context;
        this.userId = userId;
    }

    @NonNull
    @Override
    public DisconnectionGroupListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_layout_disconnection_group_list, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DisconnectionGroupListAdapter.ViewHolder holder, int position) {
        DisconnectionGroupList disconnectionGroupList = disconnectionGroupLists.get(position);

        holder.area.setText("Area: " + disconnectionGroupList.getAreaCode());
        holder.period.setText("Billing Month: " + ObjectHelpers.formatShortDate(disconnectionGroupList.getServicePeriod()));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DisconnectionListActivity.class);
                intent.putExtra("PERIOD", disconnectionGroupList.getServicePeriod());
                intent.putExtra("AREA", disconnectionGroupList.getAreaCode());
                intent.putExtra("USERID", userId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return disconnectionGroupLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public MaterialCardView cardView;
        public TextView period, area;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.parent);
            period = itemView.findViewById(R.id.servicePeriod);
            area = itemView.findViewById(R.id.areaCode);
        }
    }
}

package com.lopez.julz.readandbill.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lopez.julz.readandbill.R;
import com.lopez.julz.readandbill.dao.DisconnectionList;

import java.util.List;

public class DisconnectionListDownloadAdapter extends RecyclerView.Adapter<DisconnectionListDownloadAdapter.ViewHolder> {

    public List<DisconnectionList> disconnectionListList;
    public Context context;

    public DisconnectionListDownloadAdapter(List<DisconnectionList> disconnectionListList, Context context) {
        this.disconnectionListList = disconnectionListList;
        this.context = context;
    }

    @NonNull
    @Override
    public DisconnectionListDownloadAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_accounts_list, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DisconnectionListDownloadAdapter.ViewHolder holder, int position) {
        DisconnectionList disconnectionList = disconnectionListList.get(position);

        holder.consumerName.setText(disconnectionList.getServiceAccountName());
        holder.accountNo.setText(disconnectionList.getAccountNumber());
    }

    @Override
    public int getItemCount() {
        return disconnectionListList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView consumerName, accountNo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            consumerName = itemView.findViewById(R.id.consumerName);
            accountNo = itemView.findViewById(R.id.accountNo);
        }
    }
}

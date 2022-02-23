package com.lopez.julz.readandbill.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.lopez.julz.readandbill.DisconnectionFormActivity;
import com.lopez.julz.readandbill.R;
import com.lopez.julz.readandbill.dao.DisconnectionList;

import java.util.List;

public class DisconnectionListAdapter extends RecyclerView.Adapter<DisconnectionListAdapter.ViewHolder> {

    public List<DisconnectionList> disconnectionListList;
    public Context context;
    public String userId;

    public DisconnectionListAdapter(List<DisconnectionList> disconnectionListList, Context context, String userId) {
        this.disconnectionListList = disconnectionListList;
        this.context = context;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_account_disco_list, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DisconnectionList disconnectionList = disconnectionListList.get(position);

        holder.consumerName.setText(disconnectionList.getServiceAccountName());
        holder.accountNo.setText("Acct. No: " + disconnectionList.getAccountNumber() + " | Bill No: " + disconnectionList.getBillNumber() + " | Seq. No:" + disconnectionList.getSequenceCode());

        if (disconnectionList.getIsUploaded() != null) {
            if (disconnectionList.getIsUploaded().equals("UPLOADABLE")) {
                holder.accountStatus.setBackgroundResource(R.drawable.ic_baseline_check_circle_18);
            } else {
                holder.accountStatus.setBackgroundResource(R.drawable.ic_baseline_info_18);
            }
        } else {
            holder.accountStatus.setBackgroundResource(R.drawable.ic_baseline_info_18);
        }

        holder.accountParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DisconnectionFormActivity.class);
                intent.putExtra("USERID", userId);
                intent.putExtra("ACCTNO", disconnectionList.getAccountNumber());
                intent.putExtra("PERIOD", disconnectionList.getServicePeriod());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return disconnectionListList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView consumerName, accountNo;
        public MaterialCardView accountParent;
        public ImageView accountStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            consumerName = itemView.findViewById(R.id.consumerName);
            accountNo = itemView.findViewById(R.id.accountNo);
            accountParent = itemView.findViewById(R.id.accountParent);
            accountStatus = itemView.findViewById(R.id.accountStatus);
        }
    }
}

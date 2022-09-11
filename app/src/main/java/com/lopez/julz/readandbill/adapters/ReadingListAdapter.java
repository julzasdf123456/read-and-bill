package com.lopez.julz.readandbill.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.card.MaterialCardView;
import com.lopez.julz.readandbill.R;
import com.lopez.julz.readandbill.ReadingConsoleActivity;
import com.lopez.julz.readandbill.ReadingListViewActivity;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.ReadingSchedules;
import com.lopez.julz.readandbill.helpers.AlertHelpers;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;

import java.util.List;

public class ReadingListAdapter extends RecyclerView.Adapter<ReadingListAdapter.ViewHolder>{

    public List<ReadingSchedules> readingSchedulesList;
    public Context context;
    public String userId;

    public ReadingListAdapter(List<ReadingSchedules> readingSchedulesList, Context context, String userId) {
        this.readingSchedulesList = readingSchedulesList;
        this.context = context;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_layout_reading_schedules, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReadingSchedules readingSchedule = readingSchedulesList.get(position);

        holder.area.setText("Area " + readingSchedule.getAreaCode() + " | Day " + readingSchedule.getGroupCode());
        holder.billingMonth.setText("Billing Month: " + ObjectHelpers.formatShortDate(readingSchedule.getServicePeriod()));
        holder.scheduledDate.setText("Scheduled On: " + ObjectHelpers.formatShortDateWithDate(readingSchedule.getScheduledDate()));

        if (readingSchedule.getDisabled() != null && readingSchedule.getDisabled().equals("Yes")) {
            holder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    EditText code = new EditText(context);
                    code.setHint("Enter Code Here");

                    builder.setTitle("Reading Schedule Locked")
                            .setMessage("You need to finish the previous 2 days of your reading schedule before proceeding to this one. To unlock this, input the code provided by your DATA ADMIN below.")
                            .setView(code)
                            .setPositiveButton("PROCEED", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String idCode = readingSchedule.getId().split("-")[0];
                                    if (idCode != null && idCode.equals(code.getText().toString())) {
                                        new UpdateScheduleUnclock().execute(readingSchedule);
                                    } else {
                                        AlertHelpers.showMessageDialog(context, "Invalid Code", "Provide the code that your Data Admin has given you to continue.");
                                    }
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        } else {
            holder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ReadingListViewActivity.class);
                    intent.putExtra("USERID", userId);
                    intent.putExtra("AREACODE", readingSchedule.getAreaCode());
                    intent.putExtra("GROUPCODE", readingSchedule.getGroupCode());
                    intent.putExtra("SERVICEPERIOD", readingSchedule.getServicePeriod());
                    context.startActivity(intent);
                    Log.e("TEST", readingSchedule.getServicePeriod());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return readingSchedulesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public MaterialCardView parent;
        public TextView area, billingMonth, scheduledDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            parent = itemView.findViewById(R.id.parent);
            area = itemView.findViewById(R.id.area);
            billingMonth = itemView.findViewById(R.id.billingMonth);
            scheduledDate = itemView.findViewById(R.id.scheduledDate);
        }
    }

    public class UpdateScheduleUnclock extends AsyncTask<ReadingSchedules, Void, Void> {

        AppDatabase db;
        ReadingSchedules rs;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            db = Room.databaseBuilder(context, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();
        }

        @Override
        protected Void doInBackground(ReadingSchedules... readingSchedules) {
            try {
                if (readingSchedules != null) {
                    rs = readingSchedules[0];
                    rs.setDisabled("Unlocked");
                    db.readingSchedulesDao().updateAll(rs);
                }
            } catch (Exception e) {
                Log.e("ERR_UPDT_UNLCK_SCHD", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            Intent intent = new Intent(context, ReadingListViewActivity.class);
            intent.putExtra("USERID", userId);
            intent.putExtra("AREACODE", rs.getAreaCode());
            intent.putExtra("GROUPCODE", rs.getGroupCode());
            intent.putExtra("SERVICEPERIOD", rs.getServicePeriod());
            context.startActivity(intent);
        }
    }
}

package com.lopez.julz.readandbill.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.lopez.julz.readandbill.R;
import com.lopez.julz.readandbill.api.RequestPlaceHolder;
import com.lopez.julz.readandbill.api.RetrofitBuilder;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.DownloadedPreviousReadings;
import com.lopez.julz.readandbill.dao.DownloadedPreviousReadingsDao;
import com.lopez.julz.readandbill.dao.Rates;
import com.lopez.julz.readandbill.dao.ReadingSchedules;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadReadingListAdapter extends RecyclerView.Adapter<DownloadReadingListAdapter.ViewHolder> {

    public List<ReadingSchedules> readingSchedulesList;
    public Context context;

    public RetrofitBuilder retrofitBuilder;
    private RequestPlaceHolder requestPlaceHolder;

    public AppDatabase db;

    public DownloadReadingListAdapter(List<ReadingSchedules> readingSchedulesList, Context context) {
        this.readingSchedulesList = readingSchedulesList;
        this.context = context;

        retrofitBuilder = new RetrofitBuilder();
        requestPlaceHolder = retrofitBuilder.getRetrofit().create(RequestPlaceHolder.class);

        db = Room.databaseBuilder(context, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();
    }

    @NonNull
    @Override
    public DownloadReadingListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_layout_download_reading_schedules, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadReadingListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ReadingSchedules readingSchedule = readingSchedulesList.get(position);

        holder.area.setText("Area " + readingSchedule.getAreaCode() + " | Day " + readingSchedule.getGroupCode());
        holder.billingMonth.setText("Billing Month: " + ObjectHelpers.formatShortDate(readingSchedule.getServicePeriod()));
        holder.scheduledDate.setText("Scheduled On: " + ObjectHelpers.formatShortDateWithDate(readingSchedule.getScheduledDate()));

        holder.downloadReadingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.downloadReadingList.setEnabled(false);
                new DownloadSchedule().execute(readingSchedule);
                fetchDownloadbleList(readingSchedule, holder.downloadProgress, holder.downloadReadingList, position);
                fetchRates(readingSchedule.getServicePeriod());
            }
        });
    }

    @Override
    public int getItemCount() {
        return readingSchedulesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public MaterialCardView parent;
        public TextView area, billingMonth, scheduledDate;
        public FloatingActionButton downloadReadingList;
        public CircularProgressIndicator downloadProgress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            parent = itemView.findViewById(R.id.parent);
            area = itemView.findViewById(R.id.area);
            billingMonth = itemView.findViewById(R.id.billingMonth);
            scheduledDate = itemView.findViewById(R.id.scheduledDate);
            downloadReadingList = itemView.findViewById(R.id.downloadReadingList);
            downloadProgress = itemView.findViewById(R.id.downloadProgress);
        }
    }

    public void fetchDownloadbleList(ReadingSchedules readingSchedules, CircularProgressIndicator downloadProgress, FloatingActionButton downloadFab, int position) {
        try {
            Call<List<DownloadedPreviousReadings>> downloadCall = requestPlaceHolder.downloadAccounts(readingSchedules.getAreaCode(), readingSchedules.getGroupCode(), readingSchedules.getServicePeriod());

            downloadCall.enqueue(new Callback<List<DownloadedPreviousReadings>>() {
                @Override
                public void onResponse(Call<List<DownloadedPreviousReadings>> call, Response<List<DownloadedPreviousReadings>> response) {
                    if (response.isSuccessful()) {
                        List<DownloadedPreviousReadings> downloadedPreviousReadingsList = response.body();
                        DownloadList downloadList = new DownloadList(downloadProgress, downloadFab, downloadedPreviousReadingsList.size(), readingSchedules.getId(), position);
                        downloadList.execute(downloadedPreviousReadingsList);
                    } else {
                        Log.e("ERR_FETCH_DATA", response.errorBody() + "");
                        Toast.makeText(context, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<DownloadedPreviousReadings>> call, Throwable t) {
                    Log.e("ERR_FETCH_DATA", t.getMessage());
                    Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("ERR_FETCH_DATA", e.getMessage());
            Toast.makeText(context, "Error fetching data", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Download the schedule
     */
    public class DownloadSchedule extends AsyncTask<ReadingSchedules, Void, Void> {

        @Override
        protected Void doInBackground(ReadingSchedules... readingSchedules) {
            try {
                if (readingSchedules != null) {
                    ReadingSchedules rd = readingSchedules[0];
                    if (db.readingSchedulesDao().getOne(rd.getId()) == null) {
                        // ADD NEW
                        db.readingSchedulesDao().insertAll(rd);
                    } else {
                        // UPDATE
                        db.readingSchedulesDao().updateAll(rd);
                    }
                }
            } catch (Exception e) {
                Log.e("ERR_SV_SCHEDULE", e.getMessage());
            }
            return null;
        }
    }

    /**
     * Download Rates
     */
    public void fetchRates(String servicePeriod) {
        try {
            Call<List<Rates>> ratesCall = requestPlaceHolder.downloadRates(servicePeriod);
            ratesCall.enqueue(new Callback<List<Rates>>() {
                @Override
                public void onResponse(Call<List<Rates>> call, Response<List<Rates>> response) {
                    if (response.isSuccessful()) {
                        new SaveRates().execute(response.body());
                    }
                }

                @Override
                public void onFailure(Call<List<Rates>> call, Throwable t) {
                    Log.e("ERR_GET_RATE", t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("ERR_GET_RATE", e.getMessage());
        }
    }

    /**
     * SAVE RATES
     */
    public class SaveRates extends AsyncTask<List<Rates>, Integer, Void> {

        @Override
        protected Void doInBackground(List<Rates>... lists) {
            try {
                if (lists != null) {
                    List<Rates> ratesList = lists[0];

                    for (Rates rate : ratesList) {
                        if (db.ratesDao().getOneById(rate.getId()) != null) {
                            // update
                            db.ratesDao().updateAll(rate);
                        } else {
                            //save
                            db.ratesDao().insertAll(rate);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("ERR_SAVE_RATE", e.getMessage());
            }
            return null;
        }
    }

    /**
     * Download the previous readings
     */
    public class DownloadList extends AsyncTask<List<DownloadedPreviousReadings>, Integer, Void> {

        public CircularProgressIndicator indicator;
        public FloatingActionButton downloadFab;
        int max, position;
        String id;

        public DownloadList(CircularProgressIndicator indicator, FloatingActionButton downloadFab, int max, String id, int position) {
            this.indicator = indicator;
            this.downloadFab = downloadFab;
            this.max = max;
            this.id = id;
            this.position = position;
        }

        @Override
        protected Void doInBackground(List<DownloadedPreviousReadings>... lists) {
            try {
                if (lists != null) {
                    /**
                     * SAVE THE LIST TO DATABASE
                     */
                    DownloadedPreviousReadingsDao dpr = db.downloadedPreviousReadingsDao();
                    List<DownloadedPreviousReadings> dprList = lists[0];
                    for (int i=0; i<max; i++) {
                        DownloadedPreviousReadings downloadedPreviousReading = dpr.getOne(dprList.get(i).getId());
                        if (downloadedPreviousReading != null) {
                            // UPDATE
                            dpr.updateAll(downloadedPreviousReading);
                        } else {
                            // SAVE
                            dpr.insertAll(dprList.get(i));

                        }
                        publishProgress(i+1);
                    }
                }
            } catch (Exception e) {
                Log.e("ERR_DWNLD_LST_ADPTER", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            indicator.setMax(max);
            Toast.makeText(context, "Download started. Please don't exit this window.", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values[0] == max) {
                Toast.makeText(context, "Download complete.", Toast.LENGTH_SHORT).show();
                indicator.setProgress(0);
                downloadFab.setVisibility(View.GONE);
            } else {
                indicator.setProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            /**
             * UPDATE STATUS
             */
            try {
                Call<String> stringCall = requestPlaceHolder.updateDownloadedStatus(id);

                stringCall.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            Log.e("RESPONSE", response.body());
                            readingSchedulesList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, readingSchedulesList.size());
                        } else {
                            Log.e("RESPONSE", response.errorBody() + "");
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.e("ERR_UPDT_STSTS", t.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e("ERR_UPDT_STSTS", e.getMessage());
            }
        }
    }
}

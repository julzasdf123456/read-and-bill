package com.lopez.julz.readandbill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.lopez.julz.readandbill.api.RequestPlaceHolder;
import com.lopez.julz.readandbill.api.RetrofitBuilder;
import com.lopez.julz.readandbill.dao.AppDatabase;
import com.lopez.julz.readandbill.dao.Bills;
import com.lopez.julz.readandbill.dao.ReadingImages;
import com.lopez.julz.readandbill.dao.Readings;
import com.lopez.julz.readandbill.helpers.AlertHelpers;
import com.lopez.julz.readandbill.helpers.ObjectHelpers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadReadingsActivity extends AppCompatActivity {

    public Toolbar uploadToolbar;
    public TextView uploadbleReadings, uploadbleBills, uploadStatusText, uploadbleImages;
    public CircularProgressIndicator uploadProgress;
    public FloatingActionButton uploadButton;

    public List<Readings> readingsList;
    public List<Bills> billsList;
    public List<ReadingImages> imagesList;

    public AppDatabase db;

    public RetrofitBuilder retrofitBuilder;
    private RequestPlaceHolder requestPlaceHolder;

    boolean isUploadActive = false;
    int progress = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_readings);

        uploadToolbar = findViewById(R.id.uploadToolbar);
        setSupportActionBar(uploadToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        db = Room.databaseBuilder(this, AppDatabase.class, ObjectHelpers.dbName()).fallbackToDestructiveMigration().build();

        retrofitBuilder = new RetrofitBuilder();
        requestPlaceHolder = retrofitBuilder.getRetrofit().create(RequestPlaceHolder.class);

        uploadbleReadings = findViewById(R.id.uploadbleReadings);
        uploadbleImages = findViewById(R.id.uploadbleImages);
        uploadbleBills = findViewById(R.id.uploadbleBills);
        uploadStatusText = findViewById(R.id.uploadStatusText);
        uploadProgress = findViewById(R.id.uploadProgress);
        uploadButton = findViewById(R.id.uploadButton);
        readingsList = new ArrayList<>();
        billsList = new ArrayList<>();
        imagesList = new ArrayList<>();

        new FetchData().execute();

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadButton.setEnabled(false);
                uploadStatusText.setText("Uploading Readings...");
                uploadReadings();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!isUploadActive) {
            super.onBackPressed();
        }
    }

    public class FetchData extends AsyncTask<Void, Void, Void> {

        String totalReadings, totalBills, totalImages;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                billsList = db.billsDao().getUploadables();
                readingsList = db.readingsDao().getUploadables();
                imagesList = db.readingImagesDao().getUploadable();

                totalBills = billsList.size() + "";
                totalReadings = readingsList.size() + "";
                totalImages = imagesList.size() + "";
            } catch (Exception e) {
                Log.e("ERR_FETCH_UPLDBS", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            uploadbleReadings.setText("Total Uploadable Readings: " + totalReadings);
            uploadbleBills.setText("Total Uploadable Bills: " + totalBills);
            uploadbleImages.setText("Total Uploadable Images: " + totalImages);

            if (totalReadings.equals("0") && totalBills.equals("0") && totalImages.equals("0")) {
                uploadButton.setEnabled(false);
            } else {
                uploadButton.setEnabled(true);
                uploadProgress.setMax(readingsList.size());
            }
        }
    }

    public void uploadReadings() {
        try {
            if (readingsList.size() > 0) {
                Call<Readings> readingsCall = requestPlaceHolder.uploadReadings(readingsList.get(0));
                Log.e("UPLD_READING", readingsList.get(0).getAccountNumber());

                readingsCall.enqueue(new Callback<Readings>() {
                    @Override
                    public void onResponse(Call<Readings> call, Response<Readings> response) {
                        if (response.isSuccessful()) {
                            new UpdateReadings().execute(readingsList.get(0));
                            uploadProgress.setProgress(progress);

                            readingsList.remove(0);

                            progress += 1;

                            if (readingsList.size() > 0) {
                                uploadReadings();
                            } else {
                                progress = 1;
                                uploadProgress.setProgress(0);
                                uploadProgress.setMax(billsList.size());
                                uploadStatusText.setText("Uploading Bills...");
                                uploadBills();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Readings> call, Throwable t) {
                        Log.e("ERR_UPLD_READINGS", t.getMessage());
                        AlertHelpers.showMessageDialog(UploadReadingsActivity.this, "Readings Upload Failed", t.getMessage());
                    }
                });
            } else {
                progress = 1;
                uploadProgress.setProgress(0);
                uploadProgress.setMax(billsList.size());
                uploadStatusText.setText("Uploading Bills...");
                uploadBills();
            }

        } catch (Exception e) {
            Log.e("ERR_UPLD_READINGS", e.getMessage());
            AlertHelpers.showMessageDialog(this, "Readings Upload Failed", e.getMessage());
        }
    }

    public void uploadBills() {
        try {
            if (billsList.size() > 0) {
                Call<Bills> billsCall = requestPlaceHolder.uploadBills(billsList.get(0));
                Log.e("UPLD_BILL", billsList.get(0).getAccountNumber() + " - " + billsList.get(0).getNetAmount());


                billsCall.enqueue(new Callback<Bills>() {
                    @Override
                    public void onResponse(Call<Bills> call, Response<Bills> response) {
                        if (response.isSuccessful()) {
                            new UpdateBills().execute(billsList.get(0));
                            uploadProgress.setProgress(progress);

                            billsList.remove(0);

                            progress += 1;

                            if (billsList.size() > 0) {
                                uploadBills();
                            } else {
                                progress = 1;
                                uploadProgress.setProgress(0);
                                uploadProgress.setMax(billsList.size());
                                uploadStatusText.setText("Uploading Images...");
                                uploadImages();
                            }
                        } else {
                            try {
                                Log.e("ERR_UPLD_BILLS", response.raw() + "\n" + response.errorBody().string() );
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                AlertHelpers.showMessageDialog(UploadReadingsActivity.this, "Bills Upload Failed", response.raw() + "\n" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Bills> call, Throwable t) {
                        Log.e("ERR_UPLD_BILLS", t.getMessage());
                        AlertHelpers.showMessageDialog(UploadReadingsActivity.this, "Bills Upload Failed", t.getMessage());
                    }
                });
            } else {
                progress = 1;
                uploadProgress.setProgress(0);
                uploadProgress.setMax(billsList.size());
                uploadStatusText.setText("Uploading Images...");
                uploadImages();
            }
        } catch (Exception e) {
            Log.e("ERR_UPLD_BILLS", e.getMessage());
            AlertHelpers.showMessageDialog(this, "Bills Upload Failed", e.getMessage());
        }
    }

    public void uploadImages() {
        try {
            if (imagesList.size() > 0) {
                ReadingImages ri = imagesList.get(0);
                File imgFile = new File(ri.getPhoto());

                if (imgFile.exists()) {
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imgFile);

                    MultipartBody.Part fileBody = MultipartBody.Part.createFormData("file", imgFile.getName(), requestFile);

                    Call<ResponseBody> uploadCall = requestPlaceHolder.saveReadingImages(ri.getId(), ri.getServicePeriod(), ri.getAccountNumber(), fileBody);

                    uploadCall.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                new UpdateImages().execute(ri);
                                uploadProgress.setProgress(progress);

                                imagesList.remove(0);

                                progress += 1;

                                if (imagesList.size() > 0) {
                                    uploadImages();
                                } else {
                                    progress = 1;
                                    uploadProgress.setProgress(0);
                                    uploadStatusText.setText("Uploading Complete");
                                }
                            } else {
                                try {
                                    Log.e("ERR_UPLD_IMGS", response.raw() + "" + response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e("ERR_UPLD_IMGS", t.getMessage());
                            AlertHelpers.showMessageDialog(UploadReadingsActivity.this, "Images Upload Failed", t.getMessage());
                        }
                    });
                } else {
                    uploadProgress.setProgress(progress);

                    imagesList.remove(0);

                    progress += 1;

                    if (imagesList.size() > 0) {
                        uploadImages();
                    } else {
                        progress = 1;
                        uploadProgress.setProgress(0);
                        uploadStatusText.setText("Uploading Complete");
                    }
                }
            } else {
                progress = 1;
                uploadProgress.setProgress(0);
                uploadStatusText.setText("Uploading Complete");
            }

        } catch (Exception e) {
            Log.e("ERR_UPLD_IMGS", e.getMessage());
            AlertHelpers.showMessageDialog(this, "Images Upload Failed", e.getMessage());
        }
    }

    public class UpdateReadings extends AsyncTask<Readings, Void, Void> {

        @Override
        protected Void doInBackground(Readings... readings) {
            try {
                if (readings != null) {
                    Readings reading = readings[0];
                    reading.setUploadStatus("UPLOADED");
                    db.readingsDao().updateAll(reading);
                }
            } catch (Exception e) {
                Log.e("ERR_UPDT_READNGS", e.getMessage());
            }
            return null;
        }
    }

    public class UpdateBills extends AsyncTask<Bills, Void, Void> {

        @Override
        protected Void doInBackground(Bills... bills) {
            try {
                if (bills != null) {
                    Bills bill = bills[0];
                    bill.setUploadStatus("UPLOADED");
                    db.billsDao().updateAll(bill);
                }
            } catch (Exception e) {
                Log.e("ERR_UPDT_READNGS", e.getMessage());
            }
            return null;
        }
    }

    public class UpdateImages extends AsyncTask<ReadingImages, Void, Void> {

        @Override
        protected Void doInBackground(ReadingImages... readingImages) {
            try {
                if (readingImages != null) {
                    ReadingImages reading = readingImages[0];
                    reading.setUploadStatus("UPLOADED");
                    db.readingImagesDao().updateAll(reading);
                }
            } catch (Exception e) {
                Log.e("ERR_UPDT_READNGS", e.getMessage());
            }
            return null;
        }
    }
}
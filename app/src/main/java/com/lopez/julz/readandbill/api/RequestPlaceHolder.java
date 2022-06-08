package com.lopez.julz.readandbill.api;

import com.lopez.julz.readandbill.dao.Bills;
import com.lopez.julz.readandbill.dao.DisconnectionList;
import com.lopez.julz.readandbill.dao.DownloadedPreviousReadings;
import com.lopez.julz.readandbill.dao.Rates;
import com.lopez.julz.readandbill.dao.ReadingSchedules;
import com.lopez.julz.readandbill.dao.Readings;
import com.lopez.julz.readandbill.dao.TrackNames;
import com.lopez.julz.readandbill.dao.Tracks;
import com.lopez.julz.readandbill.helpers.TracksTmp;
import com.lopez.julz.readandbill.objects.Login;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RequestPlaceHolder {
    @POST("save-track-names")
    Call<TrackNames> saveTrackNames(@Body TrackNames trackNames);

    @POST("login")
    Call<Login> login(@Body Login login);

    @POST("save-tracks")
    Call<Tracks> saveTracks(@Body Tracks tracks);

    @GET("get-downloadable-tracknames")
    Call<List<TrackNames>> getDownloadableTrackNames();

    @GET("get-downloadable-tracks")
    Call<List<TracksTmp>> getDownloadableTracks(@Query("TrackNameId") String TrackNameId);

    /**
     * READ AND BILL
     */
    @GET("get-undownloaded-schedules")
    Call<List<ReadingSchedules>> getUndownloadedSchedules(@Query("MeterReaderId") String MeterReaderId);

    @GET("download-accounts")
    Call<List<DownloadedPreviousReadings>> downloadAccounts(@Query("AreaCode") String AreaCode, @Query("GroupCode") String GroupCode, @Query("ServicePeriod") String ServicePeriod, @Query("MeterReader") String MeterReader);

    @GET("download-rates")
    Call<List<Rates>> downloadRates(@Query("ServicePeriod") String ServicePeriod);

    @GET("update-downloaded-status")
    Call<String> updateDownloadedStatus(@Query("id") String id);

    @POST("receive-readings")
    Call<Readings> uploadReadings(@Body Readings readings);

    @POST("receive-bills")
    Call<Bills> uploadBills(@Body Bills bills);

    @Multipart
    @POST("save-reading-images")
    Call<ResponseBody> saveReadingImages(@Query("Id") String Id, @Query("ServicePeriod") String servicePeriod, @Query("AccountNumber") String AccountNumber, @Part MultipartBody.Part file);

    /**
     * DISCONNECTION
     */
    @GET("get-disconnection-list")
    Call<List<DisconnectionList>> getDisconnectionList(@Query("ServicePeriod") String svcPeriod, @Query("Area") String area);

    @POST("receive-disconnection-uploads")
    Call<Void> uploadDisconnection(@Body DisconnectionList disconnectionList);
}

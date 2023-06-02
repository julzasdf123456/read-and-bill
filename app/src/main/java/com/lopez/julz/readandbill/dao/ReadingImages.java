package com.lopez.julz.readandbill.dao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ReadingImages {
  @PrimaryKey
  @NonNull
  private String id;

  @ColumnInfo(name = "Photo")
  private String Photo;

  @ColumnInfo(name = "ReadingId")
  private String ReadingId;

  @ColumnInfo(name = "ServicePeriod")
  private String ServicePeriod;

  @ColumnInfo(name = "AccountNumber")
  private String AccountNumber;

  @ColumnInfo(name = "UploadStatus")
  private String UploadStatus;

  public ReadingImages() {
  }

  public ReadingImages(@NonNull String id, String photo, String readingId, String servicePeriod, String accountNumber, String uploadStatus) {
    this.id = id;
    Photo = photo;
    ReadingId = readingId;
    ServicePeriod = servicePeriod;
    AccountNumber = accountNumber;
    UploadStatus = uploadStatus;
  }

  @NonNull
  public String getId() {
    return id;
  }

  public void setId(@NonNull String id) {
    this.id = id;
  }

  public String getPhoto() {
    return Photo;
  }

  public void setPhoto(String photo) {
    Photo = photo;
  }

  public String getReadingId() {
    return ReadingId;
  }

  public void setReadingId(String readingId) {
    ReadingId = readingId;
  }

  public String getServicePeriod() {
    return ServicePeriod;
  }

  public void setServicePeriod(String servicePeriod) {
    ServicePeriod = servicePeriod;
  }

  public String getAccountNumber() {
    return AccountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    AccountNumber = accountNumber;
  }

  public String getUploadStatus() {
    return UploadStatus;
  }

  public void setUploadStatus(String uploadStatus) {
    UploadStatus = uploadStatus;
  }
}
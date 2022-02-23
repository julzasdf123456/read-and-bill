package com.lopez.julz.readandbill.dao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DownloadedPreviousReadings {
    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "ServiceAccountName")
    private String ServiceAccountName;

    @ColumnInfo(name = "Multiplier")
    private String Multiplier;

    @ColumnInfo(name = "Coreloss")
    private String Coreloss;

    @ColumnInfo(name = "AccountType")
    private String AccountType;

    @ColumnInfo(name = "AccountStatus")
    private String AccountStatus;

    @ColumnInfo(name = "AreaCode")
    private String AreaCode;

    @ColumnInfo(name = "GroupCode")
    private String GroupCode;

    @ColumnInfo(name = "Town")
    private String Town;

    @ColumnInfo(name = "Barangay")
    private String Barangay;

    @ColumnInfo(name = "Latitude")
    private String Latitude;

    @ColumnInfo(name = "Longitude")
    private String Longitude;

    @ColumnInfo(name = "OldAccountNo")
    private String OldAccountNo;

    @ColumnInfo(name = "KwhUsed")
    private String KwhUsed;

    @ColumnInfo(name = "ServicePeriod")
    private String ServicePeriod;

    @ColumnInfo(name = "SequenceCode")
    private String SequenceCode;

    @ColumnInfo(name = "Status")
    private String Status;

    @ColumnInfo(name = "SeniorCitizen")
    private String SeniorCitizen;

    @ColumnInfo(name = "Evat5Percent")
    private String Evat5Percent;

    @ColumnInfo(name = "Ewt2Percent")
    private String Ewt2Percent;

    @ColumnInfo(name = "Balance")
    private String Balance;

    @ColumnInfo(name = "ArrearsLedger")
    private String ArrearsLedger;

    public DownloadedPreviousReadings() {
    }

    public DownloadedPreviousReadings(@NonNull String id, String serviceAccountName, String multiplier, String coreloss, String accountType, String accountStatus, String areaCode, String groupCode, String town, String barangay, String latitude, String longitude, String oldAccountNo, String kwhUsed, String servicePeriod, String sequenceCode, String status, String seniorCitizen, String evat5Percent, String ewt2Percent, String balance, String arrearsLedger) {
        this.id = id;
        ServiceAccountName = serviceAccountName;
        Multiplier = multiplier;
        Coreloss = coreloss;
        AccountType = accountType;
        AccountStatus = accountStatus;
        AreaCode = areaCode;
        GroupCode = groupCode;
        Town = town;
        Barangay = barangay;
        Latitude = latitude;
        Longitude = longitude;
        OldAccountNo = oldAccountNo;
        KwhUsed = kwhUsed;
        ServicePeriod = servicePeriod;
        SequenceCode = sequenceCode;
        Status = status;
        SeniorCitizen = seniorCitizen;
        Evat5Percent = evat5Percent;
        Ewt2Percent = ewt2Percent;
        Balance = balance;
        ArrearsLedger = arrearsLedger;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getServiceAccountName() {
        return ServiceAccountName;
    }

    public void setServiceAccountName(String serviceAccountName) {
        ServiceAccountName = serviceAccountName;
    }

    public String getMultiplier() {
        return Multiplier;
    }

    public void setMultiplier(String multiplier) {
        Multiplier = multiplier;
    }

    public String getCoreloss() {
        return Coreloss;
    }

    public void setCoreloss(String coreloss) {
        Coreloss = coreloss;
    }

    public String getAccountType() {
        return AccountType;
    }

    public void setAccountType(String accountType) {
        AccountType = accountType;
    }

    public String getAccountStatus() {
        return AccountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        AccountStatus = accountStatus;
    }

    public String getAreaCode() {
        return AreaCode;
    }

    public void setAreaCode(String areaCode) {
        AreaCode = areaCode;
    }

    public String getGroupCode() {
        return GroupCode;
    }

    public void setGroupCode(String groupCode) {
        GroupCode = groupCode;
    }

    public String getTown() {
        return Town;
    }

    public void setTown(String town) {
        Town = town;
    }

    public String getBarangay() {
        return Barangay;
    }

    public void setBarangay(String barangay) {
        Barangay = barangay;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getOldAccountNo() {
        return OldAccountNo;
    }

    public void setOldAccountNo(String oldAccountNo) {
        OldAccountNo = oldAccountNo;
    }

    public String getKwhUsed() {
        return KwhUsed;
    }

    public void setKwhUsed(String kwhUsed) {
        KwhUsed = kwhUsed;
    }

    public String getServicePeriod() {
        return ServicePeriod;
    }

    public void setServicePeriod(String servicePeriod) {
        ServicePeriod = servicePeriod;
    }

    public String getSequenceCode() {
        return SequenceCode;
    }

    public void setSequenceCode(String sequenceCode) {
        SequenceCode = sequenceCode;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getSeniorCitizen() {
        return SeniorCitizen;
    }

    public void setSeniorCitizen(String seniorCitizen) {
        SeniorCitizen = seniorCitizen;
    }

    public String getEvat5Percent() {
        return Evat5Percent;
    }

    public void setEvat5Percent(String evat5Percent) {
        Evat5Percent = evat5Percent;
    }

    public String getEwt2Percent() {
        return Ewt2Percent;
    }

    public void setEwt2Percent(String ewt2Percent) {
        Ewt2Percent = ewt2Percent;
    }

    public String getBalance() {
        return Balance;
    }

    public void setBalance(String balance) {
        Balance = balance;
    }

    public String getArrearsLedger() {
        return ArrearsLedger;
    }

    public void setArrearsLedger(String arrearsLedger) {
        ArrearsLedger = arrearsLedger;
    }
}

package com.lopez.julz.readandbill.dao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DisconnectionList {
    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "BillId")
    private String BillId;

    @ColumnInfo(name = "AccountNumber")
    private String AccountNumber;

    @ColumnInfo(name = "ServiceAccountName")
    private String ServiceAccountName;

    @ColumnInfo(name = "AccountStatus")
    private String AccountStatus;

    @ColumnInfo(name = "Town")
    private String Town;

    @ColumnInfo(name = "Barangay")
    private String Barangay;

    @ColumnInfo(name = "Purok")
    private String Purok;

    @ColumnInfo(name = "AreaCode")
    private String AreaCode;

    @ColumnInfo(name = "GroupCode")
    private String GroupCode;

    @ColumnInfo(name = "KwhUsed")
    private String KwhUsed;

    @ColumnInfo(name = "EffectiveRate")
    private String EffectiveRate;

    @ColumnInfo(name = "NetAmount")
    private String NetAmount;

    @ColumnInfo(name = "AdditionalCharges")
    private String AdditionalCharges;

    @ColumnInfo(name = "Deductions")
    private String Deductions;

    @ColumnInfo(name = "BillingDate")
    private String BillingDate;

    @ColumnInfo(name = "ServiceDateFrom")
    private String ServiceDateFrom;

    @ColumnInfo(name = "ServiceDateTo")
    private String ServiceDateTo;

    @ColumnInfo(name = "DueDate")
    private String DueDate;

    @ColumnInfo(name = "ConsumerType")
    private String ConsumerType;

    @ColumnInfo(name = "MeterNumber")
    private String MeterNumber;

    @ColumnInfo(name = "ServicePeriod")
    private String ServicePeriod;

    @ColumnInfo(name = "IsUploaded")
    private String IsUploaded;

    @ColumnInfo(name = "Latitude")
    private String Latitude;

    @ColumnInfo(name = "Longitude")
    private String Longitude;

    @ColumnInfo(name = "SequenceCode")
    private String SequenceCode;

    @ColumnInfo(name = "BillNumber")
    private String BillNumber;

    /**
     * DISCONNECTION HISTORY FIELDS
     */
    @ColumnInfo(name = "LatitudeCaptured")
    private String LatitudeCaptured;

    @ColumnInfo(name = "LongitudeCaptured")
    private String LongitudeCaptured;

    @ColumnInfo(name = "DateDisconnected")
    private String DateDisconnected;

    @ColumnInfo(name = "TimeDisconnected")
    private String TimeDisconnected;

    @ColumnInfo(name = "UserId")
    private String UserId;

    public DisconnectionList() {
    }

    public DisconnectionList(@NonNull String id, String billId, String accountNumber, String serviceAccountName, String accountStatus, String town, String barangay, String purok, String areaCode, String groupCode, String kwhUsed, String effectiveRate, String netAmount, String additionalCharges, String deductions, String billingDate, String serviceDateFrom, String serviceDateTo, String dueDate, String consumerType, String meterNumber, String servicePeriod, String isUploaded, String latitude, String longitude, String sequenceCode, String billNumber, String latitudeCaptured, String longitudeCaptured, String dateDisconnected, String timeDisconnected, String userId) {
        this.id = id;
        BillId = billId;
        AccountNumber = accountNumber;
        ServiceAccountName = serviceAccountName;
        AccountStatus = accountStatus;
        Town = town;
        Barangay = barangay;
        Purok = purok;
        AreaCode = areaCode;
        GroupCode = groupCode;
        KwhUsed = kwhUsed;
        EffectiveRate = effectiveRate;
        NetAmount = netAmount;
        AdditionalCharges = additionalCharges;
        Deductions = deductions;
        BillingDate = billingDate;
        ServiceDateFrom = serviceDateFrom;
        ServiceDateTo = serviceDateTo;
        DueDate = dueDate;
        ConsumerType = consumerType;
        MeterNumber = meterNumber;
        ServicePeriod = servicePeriod;
        IsUploaded = isUploaded;
        Latitude = latitude;
        Longitude = longitude;
        SequenceCode = sequenceCode;
        BillNumber = billNumber;
        LatitudeCaptured = latitudeCaptured;
        LongitudeCaptured = longitudeCaptured;
        DateDisconnected = dateDisconnected;
        TimeDisconnected = timeDisconnected;
        UserId = userId;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getBillId() {
        return BillId;
    }

    public void setBillId(String billId) {
        BillId = billId;
    }

    public String getAccountNumber() {
        return AccountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        AccountNumber = accountNumber;
    }

    public String getServiceAccountName() {
        return ServiceAccountName;
    }

    public void setServiceAccountName(String serviceAccountName) {
        ServiceAccountName = serviceAccountName;
    }

    public String getAccountStatus() {
        return AccountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        AccountStatus = accountStatus;
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

    public String getPurok() {
        return Purok;
    }

    public void setPurok(String purok) {
        Purok = purok;
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

    public String getKwhUsed() {
        return KwhUsed;
    }

    public void setKwhUsed(String kwhUsed) {
        KwhUsed = kwhUsed;
    }

    public String getEffectiveRate() {
        return EffectiveRate;
    }

    public void setEffectiveRate(String effectiveRate) {
        EffectiveRate = effectiveRate;
    }

    public String getNetAmount() {
        return NetAmount;
    }

    public void setNetAmount(String netAmount) {
        NetAmount = netAmount;
    }

    public String getAdditionalCharges() {
        return AdditionalCharges;
    }

    public void setAdditionalCharges(String additionalCharges) {
        AdditionalCharges = additionalCharges;
    }

    public String getDeductions() {
        return Deductions;
    }

    public void setDeductions(String deductions) {
        Deductions = deductions;
    }

    public String getBillingDate() {
        return BillingDate;
    }

    public void setBillingDate(String billingDate) {
        BillingDate = billingDate;
    }

    public String getServiceDateFrom() {
        return ServiceDateFrom;
    }

    public void setServiceDateFrom(String serviceDateFrom) {
        ServiceDateFrom = serviceDateFrom;
    }

    public String getServiceDateTo() {
        return ServiceDateTo;
    }

    public void setServiceDateTo(String serviceDateTo) {
        ServiceDateTo = serviceDateTo;
    }

    public String getDueDate() {
        return DueDate;
    }

    public void setDueDate(String dueDate) {
        DueDate = dueDate;
    }

    public String getConsumerType() {
        return ConsumerType;
    }

    public void setConsumerType(String consumerType) {
        ConsumerType = consumerType;
    }

    public String getMeterNumber() {
        return MeterNumber;
    }

    public void setMeterNumber(String meterNumber) {
        MeterNumber = meterNumber;
    }

    public String getIsUploaded() {
        return IsUploaded;
    }

    public void setIsUploaded(String isUploaded) {
        IsUploaded = isUploaded;
    }

    public String getServicePeriod() {
        return ServicePeriod;
    }

    public void setServicePeriod(String servicePeriod) {
        ServicePeriod = servicePeriod;
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

    public String getSequenceCode() {
        return SequenceCode;
    }

    public void setSequenceCode(String sequenceCode) {
        SequenceCode = sequenceCode;
    }

    public String getBillNumber() {
        return BillNumber;
    }

    public void setBillNumber(String billNumber) {
        BillNumber = billNumber;
    }

    public String getLatitudeCaptured() {
        return LatitudeCaptured;
    }

    public void setLatitudeCaptured(String latitudeCaptured) {
        LatitudeCaptured = latitudeCaptured;
    }

    public String getLongitudeCaptured() {
        return LongitudeCaptured;
    }

    public void setLongitudeCaptured(String longitudeCaptured) {
        LongitudeCaptured = longitudeCaptured;
    }

    public String getDateDisconnected() {
        return DateDisconnected;
    }

    public void setDateDisconnected(String dateDisconnected) {
        DateDisconnected = dateDisconnected;
    }

    public String getTimeDisconnected() {
        return TimeDisconnected;
    }

    public void setTimeDisconnected(String timeDisconnected) {
        TimeDisconnected = timeDisconnected;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }
}

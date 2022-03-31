package com.lopez.julz.readandbill.dao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ReadingSchedules {
    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "AreaCode")
    private String AreaCode;

    @ColumnInfo(name = "GroupCode")
    private String GroupCode;

    @ColumnInfo(name = "ServicePeriod")
    private String ServicePeriod;

    @ColumnInfo(name = "ScheduledDate")
    private String ScheduledDate;

    @ColumnInfo(name = "MeterReader")
    private String MeterReader;

    @ColumnInfo(name = "Status")
    private String Status;

    @ColumnInfo(name = "created_at")
    private String created_at;

    @ColumnInfo(name = "update_at")
    private String update_at;

    @ColumnInfo(name = "Disabled")
    private String Disabled;

    public ReadingSchedules() {
    }

    public ReadingSchedules(@NonNull String id, String areaCode, String groupCode, String servicePeriod, String scheduledDate, String meterReader, String status, String created_at, String update_at, String disabled) {
        this.id = id;
        AreaCode = areaCode;
        GroupCode = groupCode;
        ServicePeriod = servicePeriod;
        ScheduledDate = scheduledDate;
        MeterReader = meterReader;
        Status = status;
        this.created_at = created_at;
        this.update_at = update_at;
        Disabled = disabled;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
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

    public String getServicePeriod() {
        return ServicePeriod;
    }

    public void setServicePeriod(String servicePeriod) {
        ServicePeriod = servicePeriod;
    }

    public String getScheduledDate() {
        return ScheduledDate;
    }

    public void setScheduledDate(String scheduledDate) {
        ScheduledDate = scheduledDate;
    }

    public String getMeterReader() {
        return MeterReader;
    }

    public void setMeterReader(String meterReader) {
        MeterReader = meterReader;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdate_at() {
        return update_at;
    }

    public void setUpdate_at(String update_at) {
        this.update_at = update_at;
    }

    public String getDisabled() {
        return Disabled;
    }

    public void setDisabled(String disabled) {
        Disabled = disabled;
    }
}

package com.lopez.julz.readandbill.objects;

public class DisconnectionGroupList {
    private String ServicePeriod, AreaCode;

    public DisconnectionGroupList(String ServicePeriod, String AreaCode) {
        this.ServicePeriod = ServicePeriod;
        this.AreaCode = AreaCode;
    }

    public String getServicePeriod() {
        return ServicePeriod;
    }

    public void setServicePeriod(String servicePeriod) {
        ServicePeriod = servicePeriod;
    }

    public String getAreaCode() {
        return AreaCode;
    }

    public void setAreaCode(String areaCode) {
        AreaCode = areaCode;
    }
}

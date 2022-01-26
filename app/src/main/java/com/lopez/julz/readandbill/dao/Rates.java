package com.lopez.julz.readandbill.dao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Rates {
    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "RateFor")
    private String RateFor;

    @ColumnInfo(name = "ConsumerType")
    private String ConsumerType;

    @ColumnInfo(name = "ServicePeriod")
    private String ServicePeriod;

    @ColumnInfo(name = "Notes")
    private String Notes;

    @ColumnInfo(name = "GenerationSystemCharge")
    private String GenerationSystemCharge;

    @ColumnInfo(name = "TransmissionDeliveryChargeKW")
    private String TransmissionDeliveryChargeKW;

    @ColumnInfo(name = "TransmissionDeliveryChargeKWH")
    private String TransmissionDeliveryChargeKWH;

    @ColumnInfo(name = "SystemLossCharge")
    private String SystemLossCharge;

    @ColumnInfo(name = "DistributionDemandCharge")
    private String DistributionDemandCharge;

    @ColumnInfo(name = "DistributionSystemCharge")
    private String DistributionSystemCharge;

    @ColumnInfo(name = "SupplyRetailCustomerCharge")
    private String SupplyRetailCustomerCharge;

    @ColumnInfo(name = "SupplySystemCharge")
    private String SupplySystemCharge;

    @ColumnInfo(name = "MeteringRetailCustomerCharge")
    private String MeteringRetailCustomerCharge;

    @ColumnInfo(name = "MeteringSystemCharge")
    private String MeteringSystemCharge;

    @ColumnInfo(name = "RFSC")
    private String RFSC;

    @ColumnInfo(name = "LifelineRate")
    private String LifelineRate;

    @ColumnInfo(name = "InterClassCrossSubsidyCharge")
    private String InterClassCrossSubsidyCharge;

    @ColumnInfo(name = "PPARefund")
    private String PPARefund;

    @ColumnInfo(name = "SeniorCitizenSubsidy")
    private String SeniorCitizenSubsidy;

    @ColumnInfo(name = "MissionaryElectrificationCharge")
    private String MissionaryElectrificationCharge;

    @ColumnInfo(name = "EnvironmentalCharge")
    private String EnvironmentalCharge;

    @ColumnInfo(name = "StrandedContractCosts")
    private String StrandedContractCosts;

    @ColumnInfo(name = "NPCStrandedDebt")
    private String NPCStrandedDebt;

    @ColumnInfo(name = "FeedInTariffAllowance")
    private String FeedInTariffAllowance;

    @ColumnInfo(name = "MissionaryElectrificationREDCI")
    private String MissionaryElectrificationREDCI;

    @ColumnInfo(name = "GenerationVAT")
    private String GenerationVAT;

    @ColumnInfo(name = "TransmissionVAT")
    private String TransmissionVAT;

    @ColumnInfo(name = "SystemLossVAT")
    private String SystemLossVAT;

    @ColumnInfo(name = "DistributionVAT")
    private String DistributionVAT;

    @ColumnInfo(name = "TotalRateVATExcluded")
    private String TotalRateVATExcluded;

    @ColumnInfo(name = "TotalRateVATIncluded")
    private String TotalRateVATIncluded;

    @ColumnInfo(name = "UserId")
    private String UserId;

    @ColumnInfo(name = "created_at")
    private String created_at;

    @ColumnInfo(name = "updated_at")
    private String updated_at;

    @ColumnInfo(name = "RealPropertyTax")
    private String RealPropertyTax;

    @ColumnInfo(name = "AreaCode")
    private String AreaCode;

    public Rates() {
    }

    public Rates(@NonNull String id, String rateFor, String consumerType, String servicePeriod, String notes, String generationSystemCharge, String transmissionDeliveryChargeKW, String transmissionDeliveryChargeKWH, String systemLossCharge, String distributionDemandCharge, String distributionSystemCharge, String supplyRetailCustomerCharge, String supplySystemCharge, String meteringRetailCustomerCharge, String meteringSystemCharge, String RFSC, String lifelineRate, String interClassCrossSubsidyCharge, String PPARefund, String seniorCitizenSubsidy, String missionaryElectrificationCharge, String environmentalCharge, String strandedContractCosts, String NPCStrandedDebt, String feedInTariffAllowance, String missionaryElectrificationREDCI, String generationVAT, String transmissionVAT, String systemLossVAT, String distributionVAT, String totalRateVATExcluded, String totalRateVATIncluded, String userId, String created_at, String updated_at, String realPropertyTax, String areaCode) {
        this.id = id;
        RateFor = rateFor;
        ConsumerType = consumerType;
        ServicePeriod = servicePeriod;
        Notes = notes;
        GenerationSystemCharge = generationSystemCharge;
        TransmissionDeliveryChargeKW = transmissionDeliveryChargeKW;
        TransmissionDeliveryChargeKWH = transmissionDeliveryChargeKWH;
        SystemLossCharge = systemLossCharge;
        DistributionDemandCharge = distributionDemandCharge;
        DistributionSystemCharge = distributionSystemCharge;
        SupplyRetailCustomerCharge = supplyRetailCustomerCharge;
        SupplySystemCharge = supplySystemCharge;
        MeteringRetailCustomerCharge = meteringRetailCustomerCharge;
        MeteringSystemCharge = meteringSystemCharge;
        this.RFSC = RFSC;
        LifelineRate = lifelineRate;
        InterClassCrossSubsidyCharge = interClassCrossSubsidyCharge;
        this.PPARefund = PPARefund;
        SeniorCitizenSubsidy = seniorCitizenSubsidy;
        MissionaryElectrificationCharge = missionaryElectrificationCharge;
        EnvironmentalCharge = environmentalCharge;
        StrandedContractCosts = strandedContractCosts;
        this.NPCStrandedDebt = NPCStrandedDebt;
        FeedInTariffAllowance = feedInTariffAllowance;
        MissionaryElectrificationREDCI = missionaryElectrificationREDCI;
        GenerationVAT = generationVAT;
        TransmissionVAT = transmissionVAT;
        SystemLossVAT = systemLossVAT;
        DistributionVAT = distributionVAT;
        TotalRateVATExcluded = totalRateVATExcluded;
        TotalRateVATIncluded = totalRateVATIncluded;
        UserId = userId;
        this.created_at = created_at;
        this.updated_at = updated_at;
        RealPropertyTax = realPropertyTax;
        AreaCode = areaCode;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getRateFor() {
        return RateFor;
    }

    public void setRateFor(String rateFor) {
        RateFor = rateFor;
    }

    public String getConsumerType() {
        return ConsumerType;
    }

    public void setConsumerType(String consumerType) {
        ConsumerType = consumerType;
    }

    public String getServicePeriod() {
        return ServicePeriod;
    }

    public void setServicePeriod(String servicePeriod) {
        ServicePeriod = servicePeriod;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public String getGenerationSystemCharge() {
        return GenerationSystemCharge;
    }

    public void setGenerationSystemCharge(String generationSystemCharge) {
        GenerationSystemCharge = generationSystemCharge;
    }

    public String getTransmissionDeliveryChargeKW() {
        return TransmissionDeliveryChargeKW;
    }

    public void setTransmissionDeliveryChargeKW(String transmissionDeliveryChargeKW) {
        TransmissionDeliveryChargeKW = transmissionDeliveryChargeKW;
    }

    public String getTransmissionDeliveryChargeKWH() {
        return TransmissionDeliveryChargeKWH;
    }

    public void setTransmissionDeliveryChargeKWH(String transmissionDeliveryChargeKWH) {
        TransmissionDeliveryChargeKWH = transmissionDeliveryChargeKWH;
    }

    public String getSystemLossCharge() {
        return SystemLossCharge;
    }

    public void setSystemLossCharge(String systemLossCharge) {
        SystemLossCharge = systemLossCharge;
    }

    public String getDistributionDemandCharge() {
        return DistributionDemandCharge;
    }

    public void setDistributionDemandCharge(String distributionDemandCharge) {
        DistributionDemandCharge = distributionDemandCharge;
    }

    public String getDistributionSystemCharge() {
        return DistributionSystemCharge;
    }

    public void setDistributionSystemCharge(String distributionSystemCharge) {
        DistributionSystemCharge = distributionSystemCharge;
    }

    public String getSupplyRetailCustomerCharge() {
        return SupplyRetailCustomerCharge;
    }

    public void setSupplyRetailCustomerCharge(String supplyRetailCustomerCharge) {
        SupplyRetailCustomerCharge = supplyRetailCustomerCharge;
    }

    public String getSupplySystemCharge() {
        return SupplySystemCharge;
    }

    public void setSupplySystemCharge(String supplySystemCharge) {
        SupplySystemCharge = supplySystemCharge;
    }

    public String getMeteringRetailCustomerCharge() {
        return MeteringRetailCustomerCharge;
    }

    public void setMeteringRetailCustomerCharge(String meteringRetailCustomerCharge) {
        MeteringRetailCustomerCharge = meteringRetailCustomerCharge;
    }

    public String getMeteringSystemCharge() {
        return MeteringSystemCharge;
    }

    public void setMeteringSystemCharge(String meteringSystemCharge) {
        MeteringSystemCharge = meteringSystemCharge;
    }

    public String getRFSC() {
        return RFSC;
    }

    public void setRFSC(String RFSC) {
        this.RFSC = RFSC;
    }

    public String getLifelineRate() {
        return LifelineRate;
    }

    public void setLifelineRate(String lifelineRate) {
        LifelineRate = lifelineRate;
    }

    public String getInterClassCrossSubsidyCharge() {
        return InterClassCrossSubsidyCharge;
    }

    public void setInterClassCrossSubsidyCharge(String interClassCrossSubsidyCharge) {
        InterClassCrossSubsidyCharge = interClassCrossSubsidyCharge;
    }

    public String getPPARefund() {
        return PPARefund;
    }

    public void setPPARefund(String PPARefund) {
        this.PPARefund = PPARefund;
    }

    public String getSeniorCitizenSubsidy() {
        return SeniorCitizenSubsidy;
    }

    public void setSeniorCitizenSubsidy(String seniorCitizenSubsidy) {
        SeniorCitizenSubsidy = seniorCitizenSubsidy;
    }

    public String getMissionaryElectrificationCharge() {
        return MissionaryElectrificationCharge;
    }

    public void setMissionaryElectrificationCharge(String missionaryElectrificationCharge) {
        MissionaryElectrificationCharge = missionaryElectrificationCharge;
    }

    public String getEnvironmentalCharge() {
        return EnvironmentalCharge;
    }

    public void setEnvironmentalCharge(String environmentalCharge) {
        EnvironmentalCharge = environmentalCharge;
    }

    public String getStrandedContractCosts() {
        return StrandedContractCosts;
    }

    public void setStrandedContractCosts(String strandedContractCosts) {
        StrandedContractCosts = strandedContractCosts;
    }

    public String getNPCStrandedDebt() {
        return NPCStrandedDebt;
    }

    public void setNPCStrandedDebt(String NPCStrandedDebt) {
        this.NPCStrandedDebt = NPCStrandedDebt;
    }

    public String getFeedInTariffAllowance() {
        return FeedInTariffAllowance;
    }

    public void setFeedInTariffAllowance(String feedInTariffAllowance) {
        FeedInTariffAllowance = feedInTariffAllowance;
    }

    public String getMissionaryElectrificationREDCI() {
        return MissionaryElectrificationREDCI;
    }

    public void setMissionaryElectrificationREDCI(String missionaryElectrificationREDCI) {
        MissionaryElectrificationREDCI = missionaryElectrificationREDCI;
    }

    public String getGenerationVAT() {
        return GenerationVAT;
    }

    public void setGenerationVAT(String generationVAT) {
        GenerationVAT = generationVAT;
    }

    public String getTransmissionVAT() {
        return TransmissionVAT;
    }

    public void setTransmissionVAT(String transmissionVAT) {
        TransmissionVAT = transmissionVAT;
    }

    public String getSystemLossVAT() {
        return SystemLossVAT;
    }

    public void setSystemLossVAT(String systemLossVAT) {
        SystemLossVAT = systemLossVAT;
    }

    public String getDistributionVAT() {
        return DistributionVAT;
    }

    public void setDistributionVAT(String distributionVAT) {
        DistributionVAT = distributionVAT;
    }

    public String getTotalRateVATExcluded() {
        return TotalRateVATExcluded;
    }

    public void setTotalRateVATExcluded(String totalRateVATExcluded) {
        TotalRateVATExcluded = totalRateVATExcluded;
    }

    public String getTotalRateVATIncluded() {
        return TotalRateVATIncluded;
    }

    public void setTotalRateVATIncluded(String totalRateVATIncluded) {
        TotalRateVATIncluded = totalRateVATIncluded;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getRealPropertyTax() {
        return RealPropertyTax;
    }

    public void setRealPropertyTax(String realPropertyTax) {
        RealPropertyTax = realPropertyTax;
    }

    public String getAreaCode() {
        return AreaCode;
    }

    public void setAreaCode(String areaCode) {
        AreaCode = areaCode;
    }
}

package com.lopez.julz.readandbill.helpers;

import android.util.Log;

import com.lopez.julz.readandbill.dao.DownloadedPreviousReadings;

public class ReadingHelpers {
    public static String getKwhUsed(DownloadedPreviousReadings dprPrev, Double current) {
        try {
            Double prev = Double.valueOf(dprPrev.getKwhUsed());
            return (current - prev) + "";
        } catch (Exception e) {
            Log.e("ERR_GET_KWH", e.getMessage());
            return "";
        }
    }
}

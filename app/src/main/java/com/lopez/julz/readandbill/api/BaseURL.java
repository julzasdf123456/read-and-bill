package com.lopez.julz.readandbill.api;

public class BaseURL {
    public static String baseUrl() { // default URL
        return "http://192.168.110.94/crm-noneco/public/api/";
    }

    public static String baseUrl(String ip) {
        return "http://" + ip + "/crm-noneco/public/api/";
    }
}

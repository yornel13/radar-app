package com.guardias.yornel.gpslocation.service;

/**
 * Created by Yornel on 20/7/2017.
 */

public class RadarService {

    private static RadarService outInstance = new RadarService();

    public static RadarService getInstance() {
        return outInstance;
    }

    private RadarService() {

    }
}

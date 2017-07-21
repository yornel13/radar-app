package com.guardias.yornel.gpslocation.entity;

import com.google.gson.Gson;
import com.guardias.yornel.gpslocation.db.DataHelper;

import io.realm.RealmObject;

/**
 * Created by Yornel on 17/7/2017.
 */

public class ControlPosition extends RealmObject {

    private Long id;
    private Double latitude;
    private Double longitude;
    private String placeName;
    private Boolean active;

    public ControlPosition() {

    }

    public ControlPosition(String placeName) {
        this.placeName = placeName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    /*@Override
    public String toString() {
        return new Gson().toJson(this);
    }*/

    public void save() {
        DataHelper.save(this);
    }

}

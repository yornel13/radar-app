package com.guardias.yornel.gpslocation.entity;

import java.util.List;

/**
 * Created by Yornel on 23/7/2017.
 */

public class Import {

    List<ControlPosition> controlPositions;
    List<User> users;
    List<Admin> admins;

    public List<ControlPosition> getControlPositions() {
        return controlPositions;
    }

    public void setControlPositions(List<ControlPosition> controlPositions) {
        this.controlPositions = controlPositions;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Admin> getAdmins() {
        return admins;
    }

    public void setAdmins(List<Admin> admins) {
        this.admins = admins;
    }

}

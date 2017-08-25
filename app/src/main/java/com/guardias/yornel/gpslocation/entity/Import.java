package com.guardias.yornel.gpslocation.entity;

import java.util.List;

/**
 * Created by Yornel on 23/7/2017.
 */

public class Import {

    List<User> users;
    List<Admin> admins;
    List<ControlPosition> controlPositions;
    List<Group> groups;
    List<Route> routes;
    List<RouteMarker> routeMarkers;
    List<RoutePosition> routePositions;

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

    public List<ControlPosition> getControlPositions() {
        return controlPositions;
    }

    public void setControlPositions(List<ControlPosition> controlPositions) {
        this.controlPositions = controlPositions;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public List<RouteMarker> getRouteMarkers() {
        return routeMarkers;
    }

    public void setRouteMarkers(List<RouteMarker> routeMarkers) {
        this.routeMarkers = routeMarkers;
    }

    public List<RoutePosition> getRoutePositions() {
        return routePositions;
    }

    public void setRoutePositions(List<RoutePosition> routePositions) {
        this.routePositions = routePositions;
    }
}

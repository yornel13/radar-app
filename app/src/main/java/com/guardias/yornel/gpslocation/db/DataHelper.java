package com.guardias.yornel.gpslocation.db;

/**
 * Created by Yornel on 19/7/2017.
 */

import com.guardias.yornel.gpslocation.entity.Admin;
import com.guardias.yornel.gpslocation.entity.ControlPosition;
import com.guardias.yornel.gpslocation.entity.Group;
import com.guardias.yornel.gpslocation.entity.Position;
import com.guardias.yornel.gpslocation.entity.Route;
import com.guardias.yornel.gpslocation.entity.RouteMarker;
import com.guardias.yornel.gpslocation.entity.RoutePosition;
import com.guardias.yornel.gpslocation.entity.User;
import com.guardias.yornel.gpslocation.entity.Watch;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class DataHelper {

    private static Realm realm;

    public static void save(final RealmObject object) {
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insertOrUpdate(object);
                }
            });
        } finally {
            if(realm != null) {
                realm.close();
            }
        }
    }

    public static void updateControlPos(final Double latitude, final Double longitude,
                              final String name, final ControlPosition position) {
        realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ControlPosition results = realm
                        .where(ControlPosition.class)
                        .equalTo("latitude", position.getLatitude())
                        .equalTo("longitude", position.getLongitude())
                        .findFirst();
                System.out.println(results);
                results.setLatitude(latitude);
                results.setLongitude(longitude);
                results.setPlaceName(name);
            }
        });
    }

    public static void deleteControlPos(final ControlPosition position) {
        realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ControlPosition results = realm
                        .where(ControlPosition.class)
                        .equalTo("latitude", position.getLatitude())
                        .equalTo("longitude", position.getLongitude())
                        .findFirst();
                results.deleteFromRealm();
            }
        });
    }

    public static User getUser(String dni) {
        realm = Realm.getDefaultInstance();
        User results = realm.where(User.class)
                .equalTo("dni", dni).findFirst();
        return results;
    }

    public static Admin getAdminByUsername(String user) {
        realm = Realm.getDefaultInstance();
        Admin results = realm.where(Admin.class)
                .equalTo("username", user).findFirst();
        return results;
    }

    public static Route findRoute(Long id) {
        realm = Realm.getDefaultInstance();
        Route results = realm.where(Route.class)
                .equalTo("id", id).findFirst();
        return results;
    }

    public static Group findGroup(Long id) {
        realm = Realm.getDefaultInstance();
        Group results = realm.where(Group.class)
                .equalTo("id", id).findFirst();
        return results;
    }

    public static ControlPosition findControlPosition(Long id) {
        realm = Realm.getDefaultInstance();
        ControlPosition results = realm.where(ControlPosition.class)
                .equalTo("id", id).findFirst();
        return results;
    }

    public static RealmResults<ControlPosition> getAllControlPositionsActive() {
        realm = Realm.getDefaultInstance();
        RealmResults<ControlPosition> results = realm
                .where(ControlPosition.class).greaterThan("id", 0).findAll();
        return results;
    }

    public static RealmResults<RoutePosition> getAllRoutePositionsByRouteId(Long routeId) {
        realm = Realm.getDefaultInstance();
        RealmResults<RoutePosition> results = realm
                .where(RoutePosition.class).equalTo("route.id", routeId).findAll();
        realm.close();
        return results;
    }

    public static ArrayList<ControlPosition> getAllControlsByUser(User user) {
        ArrayList<ControlPosition> controlPositions = new ArrayList<>();
        for (RoutePosition routePosition :
                getAllRoutePositionsByRouteId(user.getGroup().getRoute().getId())) {
            controlPositions.add(routePosition.getControlPosition());
        }
        return controlPositions;
    }

    public static RealmResults<ControlPosition> getAllControlPositions() {
        realm = Realm.getDefaultInstance();
        RealmResults<ControlPosition> results = realm
                .where(ControlPosition.class).findAll();
        realm.close();
        return results;
    }

    public static ControlPosition getControlPositionByLat(Double latitude, Double longitude) {
        realm = Realm.getDefaultInstance();
        ControlPosition results = realm.where(ControlPosition.class).equalTo("latitude", latitude)
                .equalTo("longitude", longitude).findFirst();
        realm.close();
        return results;
    }

    public static ArrayList<Position> getAllPositions() {
        realm = Realm.getDefaultInstance();
        ArrayList<Position> results = new ArrayList(realm
                .where(Position.class).findAll());
        realm.close();
        return results;
    }

    public static ArrayList<Position> getAllPositionsByStartTime(Long startTime) {
        realm = Realm.getDefaultInstance();
        ArrayList<Position> results = new ArrayList(realm
                .where(Position.class).equalTo("watch.startTime", startTime).findAll());
        realm.close();
        return results;
    }

    public static ArrayList<User> getAllUsers() {
        realm = Realm.getDefaultInstance();
        ArrayList<User> results = new ArrayList(realm
                .where(User.class).findAll());
        realm.close();
        return results;
    }

    public static ArrayList<Group> getAllGroups() {
        realm = Realm.getDefaultInstance();
        ArrayList<Group> results = new ArrayList(realm
                .where(Group.class).findAll());
        realm.close();
        return results;
    }

    public static ArrayList<Route> getAllRoutes() {
        realm = Realm.getDefaultInstance();
        ArrayList<Route> results = new ArrayList(realm
                .where(Route.class).findAll());
        realm.close();
        return results;
    }

    public static ArrayList<RouteMarker> getAllRouteMarkers() {
        realm = Realm.getDefaultInstance();
        ArrayList<RouteMarker> results = new ArrayList(realm
                .where(RouteMarker.class).findAll());
        realm.close();
        return results;
    }

    public static ArrayList<RoutePosition> getAllRoutePositions() {
        realm = Realm.getDefaultInstance();
        ArrayList<RoutePosition> results = new ArrayList(realm
                .where(RoutePosition.class).findAll());
        realm.close();
        return results;
    }

    public static ArrayList<Watch> getAllWatches() {
        realm = Realm.getDefaultInstance();
        ArrayList<Watch> results = new ArrayList(realm
                .where(Watch.class).findAll());
        realm.close();
        return results;
    }

    public static ArrayList<Watch> getAllWatchesByDNI(String dni) {
        realm = Realm.getDefaultInstance();
        ArrayList<Watch> results = new ArrayList(realm
                .where(Watch.class).equalTo("user.dni", dni).findAll());
        realm.close();
        return results;
    }

    public static ArrayList<Admin> getAllAdmins() {
        realm = Realm.getDefaultInstance();
        ArrayList<Admin> results = new ArrayList(realm
                .where(Admin.class).findAll());
        realm.close();
        return results;
    }

    public static void clearAll() {
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.delete(User.class);
                    realm.delete(Watch.class);
                    realm.delete(Position.class);
                    realm.delete(ControlPosition.class);
                }
            });
        } finally {
            if(realm != null) {
                realm.close();
            }
        }
    }

    public static void clear(final Class<?> cls) {
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.delete((Class<? extends RealmModel>) cls);
                }
            });
        } finally {
            if(realm != null) {
                realm.close();
            }
        }
    }

}

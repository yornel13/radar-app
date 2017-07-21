package com.guardias.yornel.gpslocation.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.google.gson.Gson;
import com.guardias.yornel.gpslocation.R;
import com.guardias.yornel.gpslocation.db.DataHelper;
import com.guardias.yornel.gpslocation.entity.ControlPosition;
import com.guardias.yornel.gpslocation.entity.Position;
import com.guardias.yornel.gpslocation.entity.Export;
import com.guardias.yornel.gpslocation.entity.User;
import com.guardias.yornel.gpslocation.entity.Watch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MainAdminSelectActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin_select);
        //DataHelper.clearUsers(); //TODO: solo para limpieza
    }

    public void goMap(View view) {
        startActivity(new Intent(this, AdminActivity.class));
    }

    public void do_import(View view) {

    }

    public void do_export(View view) {

        Export export = new Export();
        export.setControlPositions(realm.copyFromRealm(DataHelper.getAllControlPositions()));
        List<Watch> watches = realm.copyFromRealm(DataHelper.getAllWatches());

        for (Watch watch: watches) {
            watch.setPositionsList(realm.copyFromRealm(DataHelper
                    .getAllPositionsByStartTime(watch.getStartTime())));
            //////////////////////////////////////////////////////////////
            System.out.println(watch.getStartTime());
            System.out.println("cantidad de posiciones: "
                    +watch.getPositionsList().size()+" para "+watch.getUser().getDni());
            /////////////////////////////////////////////////////////////////
            for (Position position: watch.getPositionsList()) {
                position.setWatch(null);
                System.out.println(position.getControlPosition().getPlaceName());
            }
        }
        export.setWatches(watches);
        String json = new Gson().toJson(export);
        System.out.println("Tamano de String: "+json.length());

        writeToFile(json);
    }


    @Override
    protected void onResume() {
        super.onResume();

        System.out.println("Tamano users "+DataHelper.getAllUsers().size());
        System.out.println("Tamano watchs "+DataHelper.getAllWatches().size());
        System.out.println("Tamano positions "+DataHelper.getAllPositions().size());
        System.out.println("Tamano Control positions "+DataHelper.getAllControlPositions().size());
        System.out.println("positions "+DataHelper.getAllPositions().toString());
    }

    public void writeToFile(String mJsonResponse) {
        try {
            File folderPath = new File(Environment
                    .getExternalStorageDirectory().getPath()+"/databases");
            File myPath = new File(folderPath, "radar.json");
            FileWriter output = new FileWriter(myPath);
            output.write(mJsonResponse);
            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void go_map_guard(View view) {
        User user = new User();
        user.setDni("20356841");
        user.setName("Yornel");
        user.setLastname("Marval");
        preferences.save(user);
        startActivity(new Intent(this, GuardActivity.class));
    }

    public void up(View view) {
        preferences.clearUser();
    }

    public void ud(View view) {
        DataHelper.clear(User.class);
    }

    public void wp(View view) {
        preferences.clearWatchs();
    }

    public void wd(View view) {
        DataHelper.clear(Watch.class);
    }

    public void pp(View view) {
        preferences.clearPositions();
    }

    public void pd(View view) {
        DataHelper.clear(Position.class);
    }

    public void cp(View view) {
    }

    public void cd(View view) {
        DataHelper.clear(ControlPosition.class);
    }

}

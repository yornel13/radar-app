package com.guardias.yornel.gpslocation.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.google.gson.Gson;
import com.guardias.yornel.gpslocation.R;
import com.guardias.yornel.gpslocation.db.DataHelper;
import com.guardias.yornel.gpslocation.entity.Admin;
import com.guardias.yornel.gpslocation.entity.ControlPosition;
import com.guardias.yornel.gpslocation.entity.Import;
import com.guardias.yornel.gpslocation.entity.Position;
import com.guardias.yornel.gpslocation.entity.Export;
import com.guardias.yornel.gpslocation.entity.User;
import com.guardias.yornel.gpslocation.entity.Watch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MainAdminActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);
    }

    public void doImport(View view) {

       dialogWarning();

    }

    void dialogNoFound() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setMessage(R.string.file_no_found)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // nothing to do
                    }
                }).show();
    }

    void dialogImportCompleted() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setMessage(R.string.import_completed)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // nothing to do
                    }
                }).show();
    }

    void dialogExportCompleted() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setMessage("Imformacion exportada con exito a la carpeta databases como \"radar.json\".")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // nothing to do
                    }
                }).show();
    }

    void dialogWarning() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setMessage(R.string.info_import)
                .setPositiveButton(R.string.do_import, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveJsonExport();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // nothing to do
                    }
                }).show();
    }

    void saveJsonExport() {

        String json = readFile();
        if (json != null) {
            Import imp = new Gson().fromJson(json, Import.class);

            DataHelper.clear(Admin.class);
            for (Admin admin: imp.getAdmins()) {
                DataHelper.save(admin);
            }

            DataHelper.clear(User.class);
            for (User user: imp.getUsers()) {
                DataHelper.save(user);
            }

            DataHelper.clear(ControlPosition.class);
            for (ControlPosition control: imp.getControlPositions()) {
                DataHelper.save(control);
            }

            DataHelper.clear(Position.class);
            DataHelper.clear(Watch.class);

            preferences.clearPositions();
            preferences.clearWatchs();
            preferences.clearUser();

            dialogImportCompleted();
        } else {
            dialogNoFound();
        }
    }

    public void doExport(View view) {

        Export export = new Export();
        export.setControlPositions(realm.copyFromRealm(DataHelper.getAllControlPositions()));
        List<Watch> watches = realm.copyFromRealm(DataHelper.getAllWatches());

        for (Watch watch: watches) {
            watch.setPositionsList(realm.copyFromRealm(DataHelper
                    .getAllPositionsByStartTime(watch.getStartTime())));

            for (Position position: watch.getPositionsList()) {
                position.setWatch(null);
            }
        }

        export.setWatches(watches);
        String json = new Gson().toJson(export);
        writeFile(json);
        dialogExportCompleted();
    }


    @Override
    protected void onResume() {
        super.onResume();

        System.out.println("Tamano admins "+DataHelper.getAllAdmins().size());
        System.out.println("Tamano users "+DataHelper.getAllUsers().size());
        System.out.println("Tamano watchs "+DataHelper.getAllWatches().size());
        System.out.println("Tamano positions "+DataHelper.getAllPositions().size());
        System.out.println("Tamano Control positions "+DataHelper.getAllControlPositions().size());
        System.out.println("positions "+DataHelper.getAllPositions().toString());
    }

    public void writeFile(String mJsonResponse) {
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

    public String readFile() {

        String jsonImport = null;

        File folderPath = new File(Environment
                .getExternalStorageDirectory().getPath()+"/databases");
        File myPath = new File(folderPath, "radar_export.json");

        try {

            StringBuilder text = new StringBuilder();

            BufferedReader br = new BufferedReader(new FileReader(myPath));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();

            jsonImport = text.toString();
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }

        return jsonImport;
    }

    public void goMap(View view) {
        startActivity(new Intent(this, AdminActivity.class));
    }

}

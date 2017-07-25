package com.guardias.yornel.gpslocation.db;

import android.content.Context;
import android.os.AsyncTask;

import com.guardias.yornel.gpslocation.R;
import com.guardias.yornel.gpslocation.entity.Admin;
import com.guardias.yornel.gpslocation.util.Password;

import io.realm.Realm;

/**
 * Created by Yornel on 24/7/2017.
 */

public class LoginAdminTask extends AsyncTask<Void, Void, Admin> {

    private String user;
    private String pass;
    private Integer status;
    private AdminLogin listener;
    private Context context;

    public LoginAdminTask(String user, String pass, AdminLogin listener, Context context) {
        this.user = user;
        this.pass = pass;
        this.listener = listener;
        this.context = context;
    }

    protected Admin doInBackground(Void... voids) {

        Admin admin = null;
        try {
            // Work with Realm
            if (!DataHelper.getAllAdmins().isEmpty()) {
                admin = DataHelper.getAdminByUsername(user);

                if (admin == null) {
                    status = 1;
                } else {
                    if (admin.getPassword().equals(pass)) {
                        status = 0;
                    } else {
                        status = 2;
                    }
                }
            } else {
                admin = getAdminDefault();

                if (user.equals(admin.getUsername())
                        && pass.equals(admin.getPassword())) {
                    status = 0;
                } else if (!user.equals(admin.getUsername())) {
                    status = 1;
                } else if (user.equals(admin.getUsername())
                        && !pass.equals(admin.getPassword())) {
                    status = 2;
                } else {
                    status = 3;
                }
            }

        } catch (Exception e){
            e.printStackTrace();
            status = 3;
        } finally {
            Realm.getDefaultInstance().close();
            return Realm.getDefaultInstance().copyFromRealm(admin);
        }
    }

    protected void onPostExecute(Admin admin) {
        switch (status) {
            case 0:
                listener.loginAdminSuccessful(admin);
                break;
            case 1:
                listener.loginAdminFailure(context.getString(R.string.user_login_error));
                break;
            case 2:
                listener.loginAdminFailure(context.getString(R.string.password_login_error));
                break;
            case 3:
                listener.loginAdminFailure(context.getString(R.string.login_error));
                break;
        }
    }

    Admin getAdminDefault() {
        Admin admin = new Admin();
        admin.setDni("0000");
        admin.setName("Admin");
        admin.setLastname("Admin");
        admin.setUsername("admin");
        admin.setPassword(Password.MD5("admin"));
        admin.setCreate(System.currentTimeMillis());
        admin.setUpdate(System.currentTimeMillis());
        admin.setActive(true);
        return admin;
    }

    public interface AdminLogin {

        void loginAdminSuccessful(Admin admin);

        void loginAdminFailure(String message);
    }
}

package com.guardias.yornel.gpslocation.db;

import android.content.Context;
import android.os.AsyncTask;

import com.guardias.yornel.gpslocation.R;
import com.guardias.yornel.gpslocation.entity.Admin;
import com.guardias.yornel.gpslocation.entity.User;
import com.guardias.yornel.gpslocation.util.Password;

import io.realm.Realm;

/**
 * Created by Yornel on 24/7/2017.
 */

public class LoginUserTask extends AsyncTask<Void, Void, User> {

    private String username;
    private String pass;
    private Integer status;
    private UserLogin listener;
    private Context context;

    public LoginUserTask(String user, String pass, UserLogin listener, Context context) {
        this.username = user;
        this.pass = pass;
        this.listener = listener;
        this.context = context;
    }

    protected User doInBackground(Void... voids) {

        User user = null;
        try {
            user = DataHelper.getUser(username);

            if (user == null) {
                status = 1;
            } else {
                if (user.getPassword().equals(pass)) {
                    status = 0;
                } else {
                    status = 2;
                }
            }
        } catch (Exception e){
            status = 3;
        } finally {
            Realm.getDefaultInstance().close();
            return Realm.getDefaultInstance().copyFromRealm(user);
        }
    }

    protected void onPostExecute(User user) {
        switch (status) {
            case 0:
                listener.loginUserSuccessful(user);
                break;
            case 1:
                listener.loginUserFailure(context.getString(R.string.user_login_error));
                break;
            case 2:
                listener.loginUserFailure(context.getString(R.string.password_login_error));
                break;
            case 3:
                listener.loginUserFailure(context.getString(R.string.login_error));
                break;
        }
    }

    public interface UserLogin {

        void loginUserSuccessful(User user);

        void loginUserFailure(String message);
    }
}

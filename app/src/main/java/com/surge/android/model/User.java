package com.surge.android.model;

import java.io.Serializable;

/**
 * Created by Gil on 20/02/15.
 */
public class User implements Serializable {

    String user_id;
    String username;
    String email;
    int surge_points;

    public String getUserId() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public int getSurgePoints() {
        return surge_points;
    }

}

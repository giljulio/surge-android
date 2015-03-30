package com.surge.android.model;

/**
 * Created by Gil on 20/02/15.
 */
public class DrawerHeader extends DrawerItem {

    boolean signedIn = false;
    String username;

    public DrawerHeader(boolean signedIn) {
        this.signedIn = signedIn;
    }

    public DrawerHeader(boolean signedIn, String username) {
        this.signedIn = signedIn;
        this.username = username;
    }

    public boolean isSignedIn() {
        return signedIn;
    }

    public String getUsername() {
        return username;
    }

    public void setSignedIn(boolean signedIn) {
        this.signedIn = signedIn;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

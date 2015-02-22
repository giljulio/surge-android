package io.getsurge.android.model;

/**
 * Created by Gil on 20/02/15.
 */
public class AuthenticateResponse {

    String token;
    long timestamp;
    User user;

    public String getToken() {
        return token;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public User getUser() {
        return user;
    }
}

package io.getsurge.android.net;

import android.content.Context;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by Gil on 21/12/14.
 */
public class API {

    private static final String TAG = API.class.getSimpleName();

    private static final String ROOT_URL = "https://receipts-api.herokuapp.com/";

    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_PASSWORD = "password";

    private static final String KEY_AUTH_TOKEN = "token";

    /**
     * Attempts to get an auth token from the server.
     * This shouldn't be called on the UI thread as it blocks
     * @param context
     * @param email
     * @param password
     * @return the auth token is authentication has been successful, null if it hasn't
     */
    @Nullable public static String getAuthToken(Context context, String email, String password){
        JSONObject body = new JSONObject();
        try {
            body.put(PARAM_EMAIL, email);
            body.put(PARAM_PASSWORD, password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        Volley.getInstance(context).addToRequestQueue(new JsonObjectRequest(
                Request.Method.POST, ROOT_URL + "users/", body, future, future), TAG);

        try {
            JSONObject response = future.get();
            return response.getString(KEY_AUTH_TOKEN);
        } catch (InterruptedException|ExecutionException|JSONException e) {
            return null;
        }
    }

    /**
     * Gets array of receipts linked to the account
     * @param context
     * @param authToken
     * @return an array of receipts
     */
    /*public static Receipt[] getReceipts(Context context, String authToken) throws AuthenticationException {
        RequestFuture<Receipt[]> future = RequestFuture.newFuture();
        Volley.getInstance(context).addToRequestQueue(new GsonRequest<>(
                ROOT_URL + "receipts/" + "?auth=" + authToken, Receipt.class, true, null, future, future), TAG);

        try {
            return future.get();
        } catch (InterruptedException|ExecutionException e) {
            return new Receipt[]{};
        }
    }*/
}

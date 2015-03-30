package com.surge.android.net;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.surge.android.Constants;
import com.surge.android.R;
import com.surge.android.model.AuthenticateResponse;
import com.surge.android.model.BasicVideo;
import com.surge.android.model.User;
import com.surge.android.model.Video;

/**
 * Created by Gil on 21/12/14.
 */
public class API {

    private static final String TAG = API.class.getSimpleName();

    private static final String ROOT_URL = "http://www.trysurge.com/api/";

    private static final String PARAM_USER_LOGIN = "user_login";
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
            body.put(PARAM_USER_LOGIN, email);
            body.put(PARAM_PASSWORD, password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        Volley.getInstance(context).addToRequestQueue(new JsonObjectRequest(
                Request.Method.POST, ROOT_URL + "users/authenticate", body, future, future), TAG);

        try {
            JSONObject response = future.get();
            return response.getString(KEY_AUTH_TOKEN);
        } catch (InterruptedException|ExecutionException|JSONException e) {
            return null;
        }
    }

    /**
     * Gets a list of videos
     * @param context
     * @param category of what videos should be selected
     * @param sort type
     * @param skip skip
     * @param limit limit
     * @param listener on complete
     * @param errorListener
     */
    public static void getVideos(Context context, int category, String sort, int skip, int limit,
                                 Response.Listener<ArrayList<Video>> listener, Response.ErrorListener errorListener, String tag){
        Log.d(TAG, "getVideos(category: " + category + ", sort: " + sort +  ", skip: " + skip + ", limit: " + limit + ")");
        List<NameValuePair> list = new ArrayList<>();
        if(category != -1)
            list.add(new BasicNameValuePair("category", String.valueOf(category)));
        list.add(new BasicNameValuePair("sort", sort));
        list.add(new BasicNameValuePair("skip", String.valueOf(skip)));
        list.add(new BasicNameValuePair("limit", String.valueOf(limit)));

        String params = URLEncodedUtils.format(list,"UTF-8");
        GsonRequest.Builder builder = new GsonRequest.Builder();
        builder.url(ROOT_URL + "videos?" + params)
                .clazz(Video.class)
                .successListener(listener)
                .errorListener(new ErrorListenerWrapper(errorListener));

        Volley.getInstance(context).addToRequestQueue(builder.build(), tag);
    }


    public static void getUser(Context context, String userId, Response.Listener<User> listener, Response.ErrorListener errorListener, String tag){
        GsonRequest.Builder builder = new GsonRequest.Builder();
        builder.url(ROOT_URL + "users/" + userId)
                .clazz(User.class)
                .successListener(listener)
                .errorListener(new ErrorListenerWrapper(errorListener));

        Volley.getInstance(context).addToRequestQueue(builder.build(), tag);
    }


    public static void getUserVideos(Context context, String userId, String videoType, Response.Listener<ArrayList<Video>> listener, Response.ErrorListener errorListener, String tag){
        Log.d(TAG, ROOT_URL + "users/" + userId + "/" + videoType);

        GsonRequest.Builder builder = new GsonRequest.Builder();
        builder.url(ROOT_URL + "users/" + userId + "/" + videoType)
                .clazz(Video.class)
                .successListener(listener)
                .errorListener(new ErrorListenerWrapper(errorListener));

        Volley.getInstance(context).addToRequestQueue(builder.build(), tag);
    }

    public static void attemptSignUp(Context context, String username, String email, String password, Response.Listener<AuthenticateResponse> listener, Response.ErrorListener errorListener){
        GsonRequest.Builder builder = new GsonRequest.Builder();
        builder.url(ROOT_URL + "users")
                .clazz(AuthenticateResponse.class)
                .method(Request.Method.POST)
                .addBody("username", username)
                .addBody("email", email)
                .addBody("password", password)
                .successListener(listener)
                .errorListener(new ErrorListenerWrapper(errorListener));

        Volley.getInstance(context).addToRequestQueue(builder.build(), TAG);
    }

    public static void attemptSignIn(Context context, String userLogin, String password, Response.Listener<AuthenticateResponse> listener, Response.ErrorListener errorListener){

        GsonRequest.Builder builder = new GsonRequest.Builder();
        builder.url(ROOT_URL + "users/authenticate")
                .clazz(AuthenticateResponse.class)
                .method(Request.Method.POST)
                .addBody("user_login", userLogin)
                .addBody("password", password)
                .successListener(listener)
                .errorListener(new ErrorListenerWrapper(errorListener));

        Volley.getInstance(context).addToRequestQueue(builder.build(), TAG);
    }

    public static void postVideo(Context context, Account account, String title, String url, String category, Response.Listener<BasicVideo> listener, Response.ErrorListener errorListener){

        GsonRequest.Builder builder = new GsonRequest.Builder();
        builder.url(ROOT_URL + "videos")
                .clazz(BasicVideo.class)
                .method(Request.Method.POST)
                .addBody("title", title)
                .addBody("url", url)
                .addBody("category", category)
                .successListener(listener)
                .errorListener(new ErrorListenerWrapper(errorListener));

        authenticatedRequest(context, account, builder);
    }

    public static void watchVideo(Context context, Account account, String videoId, Response.Listener<Video> listener, Response.ErrorListener errorListener){
        GsonRequest.Builder builder = new GsonRequest.Builder();
        builder.url(ROOT_URL + "videos/" + videoId + "/watched")
                .clazz(Video.class)
                .method(Request.Method.POST)
                .successListener(listener)
                .errorListener(new ErrorListenerWrapper(errorListener));

        authenticatedRequest(context, account, builder);
    }

    public static void postVote(Context context, Account account, String videoId, String type, Response.Listener<Video> listener, Response.ErrorListener errorListener){

        GsonRequest.Builder builder = new GsonRequest.Builder();
        builder.url(ROOT_URL + "videos/" + videoId + "/votes")
                .clazz(Video.class)
                .method(Request.Method.PUT)
                .addBody("vote_type", type)
                .successListener(listener)
                .errorListener(new ErrorListenerWrapper(errorListener));

        authenticatedRequest(context, account, builder);
    }

    private static void authenticatedRequest(final Context context, Account account, final GsonRequest.Builder builder){
        AccountManager.get(context).getAuthToken(account, context.getString(R.string.token_type), null, false, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bundle = future.getResult();
                    String token = bundle.getString(AccountManager.KEY_AUTHTOKEN, null);
                    Log.d(TAG, token);
                    builder.addHeader("authorization", token);

                } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                    e.printStackTrace();
                }
                Volley.getInstance(context).addToRequestQueue(builder.build(), TAG);
            }
        }, null);
    }

    private static class ErrorListenerWrapper implements Response.ErrorListener {

        Response.ErrorListener mNext;

        private ErrorListenerWrapper(Response.ErrorListener mNext) {
            this.mNext = mNext;
        }

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Log.w(TAG, volleyError.toString());

            mNext.onErrorResponse(volleyError);
        }
    };


}

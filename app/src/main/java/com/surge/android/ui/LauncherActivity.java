package com.surge.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.surge.android.Constants;
import com.surge.android.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Random;

import io.fabric.sdk.android.Fabric;

public class LauncherActivity extends ActionBarActivity {

    private static final String TAG = LauncherActivity.class.getSimpleName();

    private static final String MIXPANEL_DISTINCT_ID_NAME = "Mixpanel Distinct ID";
    private static final String ANDROID_PUSH_SENDER_ID = "645884363884";

    @InjectView(R.id.launcher_surge_logo)
    ImageView logo;

    final Handler handler = new Handler();

    MixpanelAPI mMixpanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        final String trackingDistinctId = getTrackingDistinctId();

        mMixpanel = MixpanelAPI.getInstance(this, Constants.MIX_PANEL);

        mMixpanel.getPeople().identify(trackingDistinctId);

        mMixpanel.identify(trackingDistinctId);

        mMixpanel.getPeople().initPushHandling(ANDROID_PUSH_SENDER_ID);

        try {
            final JSONObject properties = new JSONObject();
            properties.put("first opened", new Date());
            mMixpanel.registerSuperPropertiesOnce(properties);
        } catch (final JSONException e) {
            throw new RuntimeException("Could not encode hour first viewed as JSON");
        }

        try {
            final JSONObject properties = new JSONObject();
            properties.put("last opened", new Date());
            mMixpanel.track("App Usage", properties);
        } catch(final JSONException e) {
            throw new RuntimeException("Could not encode hour of the day in JSON");
        }

        mMixpanel.getPeople().increment("App Opened", 1L);

        setContentView(R.layout.activity_laucher);
        ButterKnife.inject(this);

        logo.animate().alpha(1)
                .translationY(0)
                .setInterpolator(new OvershootInterpolator())
                .setStartDelay(1100L)
                .setDuration(500).start();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, 2000);

    }

    private String getTrackingDistinctId() {
        final SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        String ret = prefs.getString(MIXPANEL_DISTINCT_ID_NAME, null);
        if (ret == null) {
            ret = generateDistinctId();
            final SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putString(MIXPANEL_DISTINCT_ID_NAME, ret);
            prefsEditor.commit();
        }

        return ret;
    }

    // These disinct ids are here for the purposes of illustration.
    // In practice, there are great advantages to using distinct ids that
    // are easily associated with user identity, either from server-side
    // sources, or user logins. A common best practice is to maintain a field
    // in your users table to store mixpanel distinct_id, so it is easily
    // accesible for use in attributing cross platform or server side events.
    private String generateDistinctId() {
        final Random random = new Random();
        final byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);
        return Base64.encodeToString(randomBytes, Base64.NO_WRAP | Base64.NO_PADDING);
    }


}

package com.surge.android.utils;

import android.app.Activity;
import android.content.Intent;

import com.surge.android.R;
import com.surge.android.ui.AuthenticatorActivity;

/**
 * Created by Gil on 22/02/15.
 */
public class IntentUtils {

    public static void openSignUpAndSignInActivity(Activity activity){
        Intent intent = new Intent(activity, AuthenticatorActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_up, R.anim.scale_down);
    }

}

package io.getsurge.android.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.getsurge.android.BuildConfig;
import io.getsurge.android.R;

public class LauncherActivity extends ActionBarActivity {

    @InjectView(R.id.launcher_surge_logo)
    ImageView logo;

    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}

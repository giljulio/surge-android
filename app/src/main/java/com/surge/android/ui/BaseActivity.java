package com.surge.android.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.surge.android.Constants;
import com.surge.android.R;


/**
 * Created by Gil on 18/12/14.
 */
public class BaseActivity extends ActionBarActivity implements NavigationDrawerCallbacks, OnAccountsUpdateListener {

    private static final String TAG = BaseActivity.class.getSimpleName();

    public static final int REQUEST_CODE_AUTH = 3453;

    protected Toolbar mActionBarToolbar;

    protected NavigationDrawerFragment mNavigationDrawerFragment;

    public MixpanelAPI mMixpanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccountManager.get(this).addOnAccountsUpdatedListener(this, null, true);
        mMixpanel = MixpanelAPI.getInstance(this, Constants.MIX_PANEL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AccountManager.get(this).removeOnAccountsUpdatedListener(this);
        mMixpanel.flush();
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {

    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);
        if(mNavigationDrawerFragment != null) {
            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
            mNavigationDrawerFragment.setup(R.id.scrimInsetsFrameLayout, drawerLayout, mActionBarToolbar);

        }
    }

    public Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position){
            case 1://surging
                if(this instanceof MainActivity){
                    MainActivity mainActivity = (MainActivity) this;
                    mainActivity.setSort("surge_rate");
                    getSupportActionBar().setTitle("Surging");
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(MainActivity.KEY_SORT, "surge_rate");
                    startActivity(intent);
                }
                break;
            case 2://new
                if(this instanceof MainActivity){
                    MainActivity mainActivity = (MainActivity) this;
                    getSupportActionBar().setTitle("New");
                    mainActivity.setSort("new");
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(MainActivity.KEY_SORT, "new");
                    startActivity(intent);
                }
                break;
            case 3://top
                if(this instanceof MainActivity){
                    MainActivity mainActivity = (MainActivity) this;
                    getSupportActionBar().setTitle("Top");
                    mainActivity.setSort("top");
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(MainActivity.KEY_SORT, "top");
                    startActivity(intent);
                }
                break;
            case 4://controversial
                if(this instanceof MainActivity){
                    MainActivity mainActivity = (MainActivity) this;
                    getSupportActionBar().setTitle("Controversial");
                    mainActivity.setSort("controversial");
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(MainActivity.KEY_SORT, "controversial");
                    startActivity(intent);
                }
                break;
            case 5://featured
                if(this instanceof MainActivity){
                    MainActivity mainActivity = (MainActivity) this;
                    getSupportActionBar().setTitle("Featured");
                    mainActivity.setSort("featured");
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(MainActivity.KEY_SORT, "featured");
                    startActivity(intent);
                }
                break;
            case 6://favorites

                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment != null && mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_AUTH:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



}

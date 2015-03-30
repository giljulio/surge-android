package com.surge.android.ui;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.melnykov.fab.FloatingActionButton;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.surge.android.R;
import com.surge.android.utils.ColorUtils;


public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @InjectView(R.id.fab)
    FloatingActionButton mFab;

    SlidingTabLayout mSlidingTabLayout;
    int[] categories_colors;

    public static final String KEY_SORT = "sort";

    private String mSort;

    @InjectView(R.id.drawer)
    DrawerLayout mDrawerLayout;

    boolean mFullscreen = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(!preferences.contains("tutorial")){
            preferences.edit().putBoolean("tutorial", true).apply();
            Intent intent = new Intent(this, TutorialActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }


        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        Intent intent = getIntent();
        mSort = !intent.hasExtra(KEY_SORT) ? "surge_rate" : intent.getStringExtra(KEY_SORT);



        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());

        mViewPager.setPageMargin((int) px);
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);

        Resources res = getResources();
        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(R.color.tab_selected_strip));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        TypedArray ta = getResources().obtainTypedArray(R.array.categories_colors);
        categories_colors = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            categories_colors[i] = ta.getColor(i, 0);
        }
        ta.recycle();

        final Window window = getWindow();
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            mToolbarContainer.setPadding(0, getStatusBarHeight(), 0, 0);
        }*/

        mNavigationDrawerFragment.updateCurrentSection(categories_colors[0]);
        mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int color = (int) new ArgbEvaluator().evaluate(positionOffset, categories_colors[position],
                        categories_colors[position == categories_colors.length - 1 ? position : position + 1]);
                mSlidingTabLayout.setBackgroundColor(color);
                mActionBarToolbar.setBackgroundColor(color);
                mNavigationDrawerFragment.updateCurrentSection(color);
                mFab.setColorNormal(color);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mDrawerLayout.setStatusBarBackgroundColor(ColorUtils.darker(color, 0.8F));
                    mDrawerLayout.invalidate();
               }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public void setSort(String sort) {
        Log.d(TAG, "setSort(" + sort + ")");
        this.mSort = sort;
        if(mSectionsPagerAdapter != null) {
            for (OnSortTypeChangedListener onSortTypeChangedListener : mSectionsPagerAdapter.getRegisteredFragments()){
                onSortTypeChangedListener.onSortChange(sort);
            }
        }
    }

    interface OnSortTypeChangedListener {
        void onSortChange(String sort);
    }

    public String getSort() {
        return mSort;
    }

    public FloatingActionButton getFab() {
        return mFab;
    }

    public SlidingTabLayout getSlidingTabLayout() {
        return mSlidingTabLayout;
    }

    @OnClick(R.id.fab)
    public void addVideo(final View view){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        float density = getResources().getDisplayMetrics().density;
        float fabSize = 56 * density;
        float marginSize = 16 * density;

        int cx = (int) (screenWidth - (fabSize / 2) - marginSize);
        int cy = (int) (screenHeight - (fabSize) - marginSize);
        Intent intent = new Intent(MainActivity.this, AddVideoActivity.class);
        intent.putExtra(AddVideoActivity.KEY_REVEAL_X_COORD, cx);
        intent.putExtra(AddVideoActivity.KEY_REVEAL_Y_COORD, cy);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        Set<VideoFeedFragment> registeredFragments = new HashSet<>();

        String[] categories;

        public Set<VideoFeedFragment> getRegisteredFragments() {
            return registeredFragments;
        }

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            categories = getResources().getStringArray(R.array.categories_labels);
        }


        @Override
        public VideoFeedFragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return VideoFeedFragment.newInstance(position - 1, categories[position], categories_colors[position]);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return categories.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            return categories[position].toUpperCase(l);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            VideoFeedFragment fragment = (VideoFeedFragment) super.instantiateItem(container, position);
            registeredFragments.add(fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

    }


    public OnBack mOnBack = new OnBack() {
        @Override
        public boolean allowedToGoBack() {
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        if(mOnBack.allowedToGoBack()) {
            super.onBackPressed();
        }
    }

    interface OnBack {
        public boolean allowedToGoBack();
    }

    public void setOnBack(OnBack mOnBack) {
        this.mOnBack = mOnBack;
    }
}

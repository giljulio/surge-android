package io.getsurge.android.ui;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
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
import android.view.Window;
import android.view.WindowManager;

import com.melnykov.fab.FloatingActionButton;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.getsurge.android.R;
import io.getsurge.android.utils.ColorUtils;


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        Intent intent = getIntent();
        mSort = !intent.hasExtra(KEY_SORT) ? "surging" : intent.getStringExtra(KEY_SORT);



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

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void setSort(String sort) {
        this.mSort = sort;
        if(mSectionsPagerAdapter != null) {
            for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
                OnSortTypeChangedListener videoFeedFragment = mSectionsPagerAdapter.getItem(i);
                videoFeedFragment.onSortChange(sort);
            }
            mSectionsPagerAdapter.notifyDataSetChanged();
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

        String[] categories;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            categories = getResources().getStringArray(R.array.categories_labels);
        }


        @Override
        public VideoFeedFragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return VideoFeedFragment.newInstance(position - 1, mSort, categories_colors[position]);
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
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

}

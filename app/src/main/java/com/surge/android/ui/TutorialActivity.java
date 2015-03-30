package com.surge.android.ui;

import java.util.Locale;

import android.animation.ArgbEvaluator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.surge.android.R;
import com.surge.android.utils.Utils;

public class TutorialActivity extends ActionBarActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @InjectView(R.id.skip) Button mSkipButton;
    @InjectView(R.id.next) Button mNextButton;
    @InjectView(R.id.tutorial_container) View mContianer;
    @InjectView(R.id.buttonbar_divider) View mButtonBarDivider;
    @InjectView(R.id.buttonbar) LinearLayout mButtonBar;


    int pages[][] = new int[][] {
            //color, image, title, sub title
            new int[]{android.R.color.white, R.drawable.web_hi_res_512, R.string.app_name, R.string.best_videos},
            new int[]{R.color.category_color_music, R.drawable.tutorial_1_2, R.string.tutorial_2_title, R.string.tutorial_2_subtitle},
            new int[]{R.color.category_color_all, R.drawable.tutorial_2, R.string.app_name, R.string.best_videos},
            new int[]{R.color.category_color_all, -1, -1, -1}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        ButterKnife.inject(this);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, 0);
            }
        });
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
            }
        });

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int currentPositionColor = getResources().getColor(pages[position][0]);
                int nextPositionColor = getResources().getColor(pages[position == pages.length - 1 ? position : position + 1][0]);

                int backgroundColor = (int) new ArgbEvaluator().evaluate(
                        positionOffset, currentPositionColor, nextPositionColor);

                mContianer.setBackgroundColor(backgroundColor);

                if (position >= mViewPager.getChildCount() - 1 && positionOffset > 0) {
                    mContianer.setAlpha(1 - positionOffset);
                }

                //
                if(position == 0){
                    int grey = (int) new ArgbEvaluator().evaluate(positionOffset, Color.DKGRAY, Color.WHITE);
                    mButtonBarDivider.setBackgroundColor(grey);
                    mButtonBar.setDividerDrawable(new ColorDrawable(grey));
                    mNextButton.setTextColor(grey);
                    mSkipButton.setTextColor(grey);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if(position == pages.length - 1){
                    finish();
                    overridePendingTransition(0, 0);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TutorialFragment.newInstance(position, pages[position][1], pages[position][2], pages[position][3]);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }
    }

    /**
     * Tutorial fragment containing on page of the tutorial
     */
    public static class TutorialFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_IMAGE_SRC = "background_color";
        private static final String ARG_TITLE = "title";
        private static final String ARG_SUB_TITLE = "subtitle";
        private static final String ARG_INDEX = "index";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static TutorialFragment newInstance(int index, @DrawableRes int imageResId, @StringRes int titleResId, @StringRes int subTitleResId) {
            TutorialFragment fragment = new TutorialFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_IMAGE_SRC, imageResId);
            args.putInt(ARG_INDEX, index);
            args.putInt(ARG_TITLE, titleResId);
            args.putInt(ARG_SUB_TITLE, subTitleResId);
            fragment.setArguments(args);
            return fragment;
        }

        @InjectView(R.id.page_image)
        ImageView mImage;

        @InjectView(R.id.title)
        TextView mTitle;

        @InjectView(R.id.subtitle)
        TextView mSubTitle;

        public TutorialFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tutorial, container, false);
            ButterKnife.inject(this, rootView);

            Bundle bundle = getArguments();
            int imageId = bundle.getInt(ARG_IMAGE_SRC);
            int titleId = bundle.getInt(ARG_TITLE);
            int subTitleId = bundle.getInt(ARG_SUB_TITLE);
            int index = bundle.getInt(ARG_INDEX);

            if(index == 0){
                /*int px = (int) Utils.convertPixelsToDp(200, getActivity());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(px, px, px, px);
                mImage.setLayoutParams(lp);*/
                mTitle.setTextColor(Color.DKGRAY);
                mSubTitle.setTextColor(Color.DKGRAY);
            }

            if(imageId > 0)
                mImage.setImageDrawable(getResources().getDrawable(imageId));
            if(titleId > 0)
                mTitle.setText(titleId);
            if(subTitleId > 0)
                mSubTitle.setText(subTitleId);
            return rootView;
        }
    }

}

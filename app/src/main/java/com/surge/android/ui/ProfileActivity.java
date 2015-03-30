package com.surge.android.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.surge.android.R;
import com.surge.android.model.User;
import com.surge.android.model.Video;
import com.surge.android.net.API;
import com.surge.android.net.Volley;

public class ProfileActivity extends BaseActivity {


    private static final String TAG = ProfileActivity.class.getSimpleName();

    public static final String ARG_USER = "user";

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

    /**
     * The tab layout for the {@link android.support.v4.view.ViewPager}
     */
    SlidingTabLayout mSlidingTabLayout;

    /**
     * User object containing all data
     */
    User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        mUser = (User) intent.getSerializableExtra(ARG_USER);

        mActionBarToolbar.setSubtitleTextColor(getResources().getColor(R.color.faded_text_color));
        getSupportActionBar().setSubtitle(mUser.getSurgePoints());
        getSupportActionBar().setTitle("@" + mUser.getUsername());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());

        mViewPager.setPageMargin((int) px);
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        Resources res = getResources();
        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(R.color.tab_selected_strip));
        mSlidingTabLayout.setViewPager(mViewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            Log.d(TAG, "Home button");
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            switch (position){
                case 0:
                    return VideoListFragment.newInstance(mUser.getUserId(), "uploads");
                case 1:
                    return VideoListFragment.newInstance(mUser.getUserId(), "uploads");
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_uploads).toUpperCase(l);
                case 1:
                    return getString(R.string.title_favourites).toUpperCase(l);
//                case 2:
//                    return getString(R.string.title_achievements).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class VideoListFragment extends Fragment implements Response.Listener<ArrayList<Video>>, Response.ErrorListener {

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_VIDEO_TYPE = "videos_type";
        private static final String ARG_USER_ID = "user_id";

        private static final String TAG = VideoListFragment.class.getSimpleName();


        private LinearLayoutManager mListLayoutManager;

        @InjectView(R.id.videoFeedList)
        RecyclerView mVideoFeedList;

        private VideoListAdapter mAdapter;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static VideoListFragment newInstance(String userId, String videoType) {
            VideoListFragment fragment = new VideoListFragment();
            Bundle args = new Bundle();
            args.putString(ARG_VIDEO_TYPE, videoType);
            args.putString(ARG_USER_ID, userId);
            fragment.setArguments(args);
            return fragment;
        }

        public VideoListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
            ButterKnife.inject(this, rootView);
            mListLayoutManager = new LinearLayoutManager(getActivity());
            mVideoFeedList.setLayoutManager(mListLayoutManager);
            mVideoFeedList.setHasFixedSize(true);
//            mVideoFeedList.setItemAnimator(new SlideInOutTopItemAnimator(mVideoFeedList));

            Bundle bundle = getArguments();
            String videoType = bundle.getString(ARG_VIDEO_TYPE);
            mAdapter = new VideoListAdapter(new ArrayList<Video>());
            mVideoFeedList.setAdapter(mAdapter);

            String userId = getArguments().getString(ARG_USER_ID);

            API.getUserVideos(getActivity(), userId, videoType/*"favourites"*/, this, this, TAG);

            return rootView;
        }


        @Override
        public void onErrorResponse(VolleyError volleyError) {

        }

        @Override
        public void onResponse(ArrayList<Video> videos) {
            Log.d(TAG, "Videos returned: " + videos.size());
            mAdapter.getVideos().addAll(videos);
            mAdapter.notifyDataSetChanged();
        }

        public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoListViewHolder> {

            private List<Video> videos;

            VideoListAdapter(List<Video> videos) {
                if (videos == null) {
                    throw new IllegalArgumentException("videos must not be null");
                }
                this.videos = videos;
            }

            public List<Video> getVideos() {
                return videos;
            }

            @Override
            public VideoListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.cell_list_video, viewGroup, false);
                return new VideoListViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(VideoListViewHolder viewHolder, int position) {
                Video video = videos.get(position);
                viewHolder.title.setText(video.getTitle());
                String thumbnailURL = "http://img.youtube.com/vi/" + video.getUrl() + "/hqdefault.jpg";
                viewHolder.thumbnail.setImageUrl(thumbnailURL, Volley.getInstance(getActivity()).getImageLoader());
            }



            @Override
            public int getItemCount() {
                return videos.size();
            }

            public final class VideoListViewHolder extends RecyclerView.ViewHolder {

                TextView title;
                NetworkImageView thumbnail;

                public VideoListViewHolder(View itemView) {
                    super(itemView);
                    title = (TextView) itemView.findViewById(R.id.cell_video_list_title);
                    thumbnail = (NetworkImageView) itemView.findViewById(R.id.cell_video_list_thumbnail);
                }
            }
        }
    }

}

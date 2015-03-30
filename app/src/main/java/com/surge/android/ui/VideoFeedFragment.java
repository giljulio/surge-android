package com.surge.android.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.surge.android.R;
import com.surge.android.model.Video;
import com.surge.android.net.API;
import com.surge.android.net.Volley;

/**
 * A placeholder fragment containing a simple view.
 */
public class VideoFeedFragment extends Fragment implements Response.Listener<ArrayList<Video>>,
        Response.ErrorListener,
        MainActivity.OnSortTypeChangedListener,
        SwipeRefreshLayout.OnRefreshListener,
        OnAccountsUpdateListener {

    private static final String TAG = VideoFeedFragment.class.getSimpleName();

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SORT_BY = "sort_by";
    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_COLOR = "color";
    private static final String ARG_CATEGORY_NAME = "category_name";

    private static final int LOAD_COUNT = 10;

    private boolean mAllLoaded = false;
    private int mFeedOffset = 0;
    private boolean mCurrentlyLoading = false;

    private int mCategoryId = -1;
    private String mCategoryName = "";
    private int mColor;

    VideoFeedAdapter mAdapter;
    GridLayoutManager mListLayoutManager;

    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;

    @InjectView(R.id.videoFeedList)
    RecyclerView mVideoFeedList;

    ObservableOnScrollListener
            mObservableOnScrollListener;


    MainActivity activity;

    boolean forceRefresh = false;

    @InjectView(R.id.refresh_layout)
    SwipeRefreshLayout mRefreshLayout;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static VideoFeedFragment newInstance(int categoryId, String category, int color) {
        VideoFeedFragment fragment = new VideoFeedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_ID, categoryId);
        args.putInt(ARG_COLOR, color);
        args.putString(ARG_CATEGORY_NAME, category);
        fragment.setArguments(args);
        return fragment;
    }


    public VideoFeedFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mCategoryId = bundle.getInt(ARG_CATEGORY_ID, -1);
        mColor = bundle.getInt(ARG_COLOR);
        mCategoryName = bundle.getString(ARG_CATEGORY_NAME);
        AccountManager.get(getActivity()).addOnAccountsUpdatedListener(this, null, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AccountManager.get(getActivity()).removeOnAccountsUpdatedListener(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);
        mListLayoutManager = new GridLayoutManager(getActivity(), 1);
        mVideoFeedList.setLayoutManager(mListLayoutManager);
        mVideoFeedList.setHasFixedSize(true);
        mVideoFeedList.setItemAnimator(new SlideInOutTopItemAnimator(mVideoFeedList));

        mAdapter = new VideoFeedAdapter((BaseActivity) getActivity(), mCategoryName, this, new ArrayList<Video>(), mColor);
        mVideoFeedList.setAdapter(mAdapter);

        mObservableOnScrollListener = new ObservableOnScrollListener();
        mVideoFeedList.setOnScrollListener(mObservableOnScrollListener);

        refresh();
        activity = ((MainActivity) getActivity());

        mRefreshLayout.setProgressViewOffset(false, 200, 400);
        mRefreshLayout.setOnRefreshListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(forceRefresh){
            mAdapter.removeFragment(null, true);
            refresh();
            forceRefresh = false;
        }
    }

    public void refresh(){
        mFeedOffset = 0;
        mAllLoaded = false;
        mCurrentlyLoading = false;
        mVideoFeedList.post(new Runnable() {
            @Override
            public void run() {
                mVideoFeedList.scrollToPosition(0);
            }
        });
        showLoading();
        mAdapter.getVideos().clear();
        Volley.getInstance(getActivity()).cancelPendingRequests(TAG + mCategoryId);
        loadMore(LOAD_COUNT);
    }


    @Override
    public void onStart() {
        super.onStart();
        HideExtraOnScroll hideExtraOnScroll = HideExtraOnScroll.getInstance((View) activity.getActionBarToolbar().getParent());
        HideExtraOnScroll fabOnScroll = HideExtraOnScroll.getFABInstance(activity.getFab());
        mObservableOnScrollListener.addScrollListener(hideExtraOnScroll);
        mObservableOnScrollListener.addScrollListener(fabOnScroll);
        mObservableOnScrollListener.addScrollListener(onScrollListener);
        hideExtraOnScroll.onScrolled(mVideoFeedList, mVideoFeedList.getScrollX(), mVideoFeedList.getScrollY());
        fabOnScroll.onScrolled(mVideoFeedList, mVideoFeedList.getScrollX(), mVideoFeedList.getScrollY());
    }



    @Override
    public void onStop() {
        super.onStop();

        // Prevent memory leaks, fuck yeah!
        HideExtraOnScroll hideExtraOnScroll = HideExtraOnScroll.getInstance((View) activity.getActionBarToolbar().getParent());
        HideExtraOnScroll fabOnScroll = HideExtraOnScroll.getFABInstance(activity.getFab());
        mObservableOnScrollListener.removeScrollListener(fabOnScroll);
        mObservableOnScrollListener.removeScrollListener(hideExtraOnScroll);
    }

    @Override
    public void onPause() {
        mAdapter.removeFragment(null, true);
        super.onPause();
    }

    private void showLoading(){
        if(mVideoFeedList.getVisibility() == View.VISIBLE)
            mVideoFeedList.setVisibility(View.GONE);
        if(mProgressBar.getVisibility() == View.GONE)
            mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading(){
        if(mVideoFeedList.getVisibility() == View.GONE)
            mVideoFeedList.setVisibility(View.VISIBLE);
        if(mProgressBar.getVisibility() == View.VISIBLE)
            mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onResponse(ArrayList<Video> videos) {
        Log.d(TAG, "onResponse: Video count: " + videos.size());
        if(videos.size() < LOAD_COUNT){
            mAllLoaded = true;
        }
        mCurrentlyLoading = false;

        mAdapter.getVideos().addAll(videos);
//        mAdapter.notifyItemRangeInserted(mFeedOffset, videos.size());
        mAdapter.notifyDataSetChanged();
        mFeedOffset += videos.size();
        hideLoading();
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        Log.e(TAG, volleyError.toString());
    }

    @Override
    public void onSortChange(String sort) {
        if(isResumed()) {
            mAdapter.removeFragment(null, true);
            refresh();
        } else {
            forceRefresh = true;
        }
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(false);
        refresh();
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        refresh();
    }


    private void loadMore(int count){
        mCurrentlyLoading = true;
        API.getVideos(getActivity(), mCategoryId, ((MainActivity) getActivity()).getSort(),
                mFeedOffset, count, this, this, TAG + mCategoryId);
    }


    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int currentPosition = mListLayoutManager.findFirstVisibleItemPosition();

            if(mAdapter.getItemCount() - (LOAD_COUNT / 2) < currentPosition && !mAllLoaded && !mCurrentlyLoading){
                Log.d(TAG, "loadMore");
                loadMore(LOAD_COUNT);
            }
        }
    };

}

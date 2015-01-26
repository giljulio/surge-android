package io.getsurge.android.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.getsurge.android.R;
import io.getsurge.android.model.NavigationItem;
import io.getsurge.android.model.Video;

/**
 * A placeholder fragment containing a simple view.
 */
public class VideoFeedFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SORT_BY = "sort_by";
    private static final String ARG_CATEGORY_ID = "category_id";

    private RecyclerView mVideoFeedList;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static VideoFeedFragment newInstance(int categoryId, String sortBy) {
        VideoFeedFragment fragment = new VideoFeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SORT_BY, sortBy);
        args.putInt(ARG_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    public VideoFeedFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mVideoFeedList = (RecyclerView) rootView.findViewById(R.id.videoFeedList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mVideoFeedList.setLayoutManager(layoutManager);
        mVideoFeedList.setHasFixedSize(true);
        mVideoFeedList.setItemAnimator(new SlideInOutTopItemAnimator(mVideoFeedList));

        ArrayList<Video> videos = new ArrayList<>();
        VideoFeedAdapter adapter = new VideoFeedAdapter(getActivity(), videos);
        mVideoFeedList.setAdapter(adapter);
        videos.add(new Video());
        adapter.notifyItemInserted(0);
        videos.add(new Video());
        adapter.notifyItemInserted(1);
        videos.add(new Video());
        adapter.notifyItemInserted(2);
        return rootView;
    }
}

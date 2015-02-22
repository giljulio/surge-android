package io.getsurge.android.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;

import io.getsurge.android.Constants;

/**
 * Created by Gil on 06/02/15.
 */

public final class VideoFragment extends YouTubePlayerSupportFragment
        implements YouTubePlayer.OnInitializedListener, View.OnClickListener {

    private static final String TAG = VideoFragment.class.getSimpleName();
    private YouTubePlayer player;
    private String videoId;
    YouTubePlayerView playerView;

    public static VideoFragment newInstance() {
        return new VideoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize(Constants.YOUTUBE_KEY, this);


        //playerView.setOnClickListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        final View rootView = super.onCreateView(layoutInflater, viewGroup, bundle);
        /*rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "onGlobalLayout");
                disableChildViews(rootView);
                rootView.setClickable(true);
                rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "onTouch");
                        player.setFullscreen(true);
                        Toast.makeText(getActivity(), "on click", Toast.LENGTH_SHORT).show();
                    }
                });
                rootView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Log.d(TAG, "onTouch");
                        Toast.makeText(getActivity(), "on touch", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });

            }
        });*/
        return rootView;
    }

    private void disableChildViews(View view){
        view.setClickable(false);
        view.setFocusable(false);
        if(view instanceof ViewGroup){
            ViewGroup viewGroup = ((ViewGroup) view);
            for (int i = 0; i < viewGroup.getChildCount(); i++)
                disableChildViews(viewGroup.getChildAt(i));
        }
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
        }
        super.onDestroy();
    }

    public void setVideoId(String videoId) {
        if (videoId != null && !videoId.equals(this.videoId)) {
            this.videoId = videoId;
            if (player != null) {
                player.loadVideo(videoId);
            }
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer player, boolean restored) {
        this.player = player;
        player.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
//        player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
//        player.setOnFullscreenListener((VideoListDemoActivity) getActivity());
        if (!restored && videoId != null) {
            player.loadVideo(videoId);
//            player.setFullscreen(true);
        }

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
        this.player = null;
    }

    public void setFullScreen(boolean fullScreen){
        this.player.setFullscreen(fullScreen);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
    }
}
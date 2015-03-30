package com.surge.android.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import com.surge.android.Constants;

/**
 * Created by Gil on 06/02/15.
 */

public final class VideoFragment extends YouTubePlayerSupportFragment
        implements YouTubePlayer.OnInitializedListener, View.OnClickListener {

    private static final String TAG = VideoFragment.class.getSimpleName();
    private YouTubePlayer player;
    private String videoId;

    public static VideoFragment newInstance() {
        return new VideoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize(Constants.YOUTUBE_KEY, this);


        //playerView.setOnClickListener(this);
    }

    GestureDetector gestureDetector;


    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown");
            return true;
        }
        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            Log.d(TAG, "Tapped at: (" + x + "," + y + ")");

            setFullScreen(true);
            /*Intent intent = new Intent(getActivity(), YouTubePlayerActivity.class);

            // Youtube video ID or Url (Required)
            intent.putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, videoId);

            // Youtube player style (DEFAULT as default)
            intent.putExtra(YouTubePlayerActivity.EXTRA_PLAYER_STYLE, YouTubePlayer.PlayerStyle.DEFAULT);

            // Screen Orientation Setting (AUTO for default)
            // AUTO, AUTO_START_WITH_LANDSCAPE, ONLY_LANDSCAPE, ONLY_PORTRAIT
            intent.putExtra(YouTubePlayerActivity.EXTRA_ORIENTATION, Orientation.AUTO);

            // Show audio interface when user adjust volume (true for default)
            intent.putExtra(YouTubePlayerActivity.EXTRA_SHOW_AUDIO_UI, true);

            // If the video is not playable, use Youtube app or Internet Browser to play it
            // (true for default)
            intent.putExtra(YouTubePlayerActivity.EXTRA_HANDLE_ERROR, true);

            // Animation when closing youtubeplayeractivity (none for default)
//            intent.putExtra(YouTubePlayerActivity.EXTRA_ANIM_ENTER, R.anim.fade_in);
//            intent.putExtra(YouTubePlayerActivity.EXTRA_ANIM_EXIT, R.anim.fade_out);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);*/


            return true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {

        gestureDetector = new GestureDetector(getActivity(), new GestureListener());

        LinearLayout container = new LinearLayout(getActivity()){
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                Log.d(TAG, "onInterceptTouchEvent");
                gestureDetector.onTouchEvent(ev);
                return false;
            }
        };

        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        final View rootView = super.onCreateView(layoutInflater, container, bundle);
        container.addView(rootView);
        return container;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

    boolean mFullscreen = false;

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer player, boolean restored) {
        this.player = player;
        player.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
            @Override
            public void onFullscreen(boolean isFullscreen) {
                player.setPlayerStyle(isFullscreen ? YouTubePlayer.PlayerStyle.DEFAULT : YouTubePlayer.PlayerStyle.MINIMAL);
                mFullscreen = isFullscreen;
            }
        });
        player.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
        if (!restored && videoId != null) {
            player.loadVideo(videoId);
        }
        ((MainActivity) getActivity()).setOnBack(new MainActivity.OnBack() {
            @Override
            public boolean allowedToGoBack() {
                if(mFullscreen && player != null){
                    setFullScreen(false);
                    return false;
                }
                return true;
            }
        });

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
        this.player = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public void setFullScreen(boolean fullScreen){
        if(player != null) {
            player.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI);
            player.setFullscreen(fullScreen);
        }
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
    }

    public YouTubePlayer getPlayer(){
        return player;
    }
}
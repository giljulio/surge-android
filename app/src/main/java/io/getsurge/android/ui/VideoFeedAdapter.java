package io.getsurge.android.ui;

import android.animation.Animator;
import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import io.getsurge.android.R;
import io.getsurge.android.model.Video;
import io.getsurge.android.net.Volley;
import io.getsurge.android.utils.Utils;

/**
 * Created by Gil on 18/01/15.
 */
public class VideoFeedAdapter extends RecyclerView.Adapter<VideoFeedAdapter.VideoViewHolder> {

    private static final int REQ_START_STANDALONE_PLAYER = 1;

    private BaseActivity mActivity;
    private VideoFeedFragment mVideoFeedFragment;
    private ArrayList<Video> mVideos;
    private int mColor;

    FragmentManager fragmentManager;
    VideoFragment mVideoFragment;
    VideoViewHolder mViewHolderVideoPlayBack;

    public VideoFeedAdapter(BaseActivity activty, VideoFeedFragment videoFeedFragment, ArrayList<Video> videos, int color) {
        mActivity = activty;
        mVideos = videos;
        mColor = color;
        mVideoFeedFragment = videoFeedFragment;
        fragmentManager = mVideoFeedFragment.getChildFragmentManager();

    }

    public ArrayList<Video> getVideos(){
        return mVideos;
    }

    @Override
    public VideoFeedAdapter.VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_feed_video, parent, false);
        return new VideoViewHolder(v);
    }

    @Override
    public void onViewRecycled(VideoViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public void removeFragment(VideoViewHolder videoViewHolder, boolean force){
        if(mVideoFragment != null && (force || mViewHolderVideoPlayBack.equals(videoViewHolder))) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.remove(mVideoFragment);
            fragmentTransaction.commit();
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            mViewHolderVideoPlayBack.title.setVisibility(View.VISIBLE);
            mViewHolderVideoPlayBack.overflow.setVisibility(View.VISIBLE);
            mVideoFragment = null;
        }
    }

    @Override
    public void onViewDetachedFromWindow(VideoViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        removeFragment(holder, false);
    }

    @Override
    public void onBindViewHolder(final VideoFeedAdapter.VideoViewHolder holder, int position) {
        final Video video = mVideos.get(position);
        holder.title.setText(video.getTitle());
        holder.downVoteTV.setText(String.valueOf(video.getDownVote()));
        holder.upVoteTV.setText(String.valueOf(video.getUpVote()));
        holder.thumbnail.setImageResource(android.R.color.transparent);
        holder.thumbnail.setImageUrl("http://img.youtube.com/vi/" + video.getUrl() + "/hqdefault.jpg",
                Volley.getInstance(mActivity).getImageLoader());

        holder.downVoteTV.setTextColor(mActivity.getResources().getColor(R.color.theme_text_primary));
        Drawable downVoteDrawable = holder.downVoteIV.getDrawable();
        downVoteDrawable.setColorFilter(null);
        holder.downVoteIV.setImageDrawable(downVoteDrawable);

        holder.upVoteTV.setTextColor(mActivity.getResources().getColor(R.color.theme_text_primary));
        Drawable drawable = holder.upVoteIV.getDrawable();
        drawable.setColorFilter(null);
        holder.upVoteIV.setImageDrawable(drawable);

        holder.thumbnail.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(mActivity,
                    new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Toast.makeText(mActivity, "Video favorited", Toast.LENGTH_SHORT).show();
                    return super.onDoubleTap(e);
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    holder.title.setVisibility(View.GONE);
                    holder.overflow.setVisibility(View.GONE);
                    removeFragment(holder, true);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    int id = Utils.generateViewId();
                    holder.videoContainer.setId(id);
                    mVideoFragment = VideoFragment.newInstance();
                    mVideoFragment.setVideoId(video.getUrl());
                    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    mViewHolderVideoPlayBack = holder;
                    fragmentTransaction.add(id, mVideoFragment);
                    fragmentTransaction.commit();
                    holder.videoContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mVideoFragment.setFullScreen(true);
                        }
                    });
                    return true;
                }
            });


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
        holder.downVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                animateView(v, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        int count = Integer.valueOf(holder.downVoteTV.getText().toString()) + 1;
                        holder.downVoteTV.setText(String.valueOf(count));
                        holder.downVoteTV.setTextColor(mColor);
                        Drawable drawable = holder.downVoteIV.getDrawable();
                        drawable.setColorFilter(mColor, PorterDuff.Mode.SRC_ATOP);
                        holder.downVoteIV.setImageDrawable(drawable);
                        return null;
                    }
                });
            }
        });
        holder.upVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
//                if(!mActivity.isAuthenticated()) {
//                    mActivity.triggerAuthenticate(mActivity);
//                } else {
                    animateView(v, new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            int count = Integer.valueOf(holder.upVoteTV.getText().toString()) + 1;
                            holder.upVoteTV.setText(String.valueOf(count));
                            holder.upVoteTV.setTextColor(mColor);
                            Drawable drawable = holder.upVoteIV.getDrawable();
                            drawable.setColorFilter(mColor, PorterDuff.Mode.SRC_ATOP);
                            holder.upVoteIV.setImageDrawable(drawable);
                            return null;
                        }
                    });
//                }
            }
        });

    }

    public void animateView(final View v, final Callable callable){
        v.animate().cancel();
        v.animate()
                .scaleX(1.2F)
                .scaleY(1.2F)
                .setDuration(200)
                .setInterpolator(new OvershootInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        try {
                            callable.call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        v.animate()
                                .scaleY(1F)
                                .scaleX(1F)
                                .setDuration(200)
                                .setInterpolator(new DecelerateInterpolator())
                                .setListener(null)
                                .start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        TextView downVoteTV, upVoteTV;
        ImageView downVoteIV, upVoteIV;
        View upVote, downVote, overflow;
        TextView title;
        NetworkImageView thumbnail;


        FrameLayout videoContainer;

        public VideoViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.cell_video_feed_title);
            thumbnail = (NetworkImageView) itemView.findViewById(R.id.cell_video_feed_thumbnail);
            upVote = itemView.findViewById(R.id.cell_video_feed_up_vote);
            downVote = itemView.findViewById(R.id.cell_video_feed_down_vote);
            overflow = itemView.findViewById(R.id.cell_video_feed_overflow);
            downVoteTV = (TextView) itemView.findViewById(R.id.cell_video_feed_down_vote_label);
            upVoteTV = (TextView) itemView.findViewById(R.id.cell_video_feed_up_vote_label);
            downVoteIV = (ImageView) itemView.findViewById(R.id.cell_video_feed_down_vote_icon);
            upVoteIV = (ImageView) itemView.findViewById(R.id.cell_video_feed_up_vote_icon);
            videoContainer = (FrameLayout) itemView.findViewWithTag("video_container_tag");
        }

    }
}

package com.surge.android.ui;

import android.accounts.Account;
import android.animation.Animator;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;

import com.surge.android.R;
import com.surge.android.model.Video;
import com.surge.android.net.API;
import com.surge.android.net.Volley;
import com.surge.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Gil on 18/01/15.
 */
public class VideoFeedAdapter extends RecyclerView.Adapter<VideoFeedAdapter.VideoViewHolder> {

    private static final int REQ_START_STANDALONE_PLAYER = 1;
    private static final String TAG = VideoFeedAdapter.class.getSimpleName();

    private BaseActivity mActivity;
    private VideoFeedFragment mVideoFeedFragment;
    private ArrayList<Video> mVideos;
    private int mColor;

    FragmentManager fragmentManager;
    VideoFragment mVideoFragment;
    VideoViewHolder mViewHolderVideoPlayBack;

    int mGreyColor;

    String mCategory;

    public VideoFeedAdapter(BaseActivity activty, String categoryName, VideoFeedFragment videoFeedFragment, ArrayList<Video> videos, int color) {
        mActivity = activty;
        mVideos = videos;
        mColor = color;
        mVideoFeedFragment = videoFeedFragment;
        fragmentManager = mVideoFeedFragment.getChildFragmentManager();
        mGreyColor = mActivity.getResources().getColor(R.color.theme_text_primary);
        mCategory = categoryName;
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
            mViewHolderVideoPlayBack.thumbnail.setClickable(true);
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
    public void onBindViewHolder(final VideoFeedAdapter.VideoViewHolder holder, final int position) {
        final Video video = mVideos.get(position);
        holder.title.setText(video.getTitle());
        /*holder.downVoteTV.setText(String.valueOf(video.getDownVote()));
        holder.upVoteTV.setText(String.valueOf(video.getUpVote()));*/

        String thumbnailURL = "http://img.youtube.com/vi/" + video.getUrl() + "/hqdefault.jpg";
        if (holder.thumbnail.getTag() == null || !holder.thumbnail.getTag().toString().equals(thumbnailURL)) {
            holder.thumbnail.setImageResource(android.R.color.transparent);
            holder.thumbnail.setTag(thumbnailURL);
            holder.thumbnail.setImageUrl(thumbnailURL, Volley.getInstance(mActivity).getImageLoader());
        }

        /*holder.downVoteTV.setTextColor();
        Drawable downVoteDrawable = holder.downVoteIV.getDrawable();
        downVoteDrawable.setColorFilter(null);
        holder.downVoteIV.setImageDrawable(downVoteDrawable);

        holder.upVoteTV.setTextColor(mActivity.getResources().getColor(R.color.theme_text_primary));
        Drawable drawable = holder.upVoteIV.getDrawable();
        drawable.setColorFilter(null);
        holder.upVoteIV.setImageDrawable(drawable);*/


        vote(holder.downVoteTV, holder.downVoteIV, video.getDownVote(), false);
        vote(holder.upVoteTV, holder.upVoteIV, video.getUpVote(), false);

        holder.uploaderUsernameTV.setText("@" + video.getUploader().getUsername());
        holder.uploaderUsernameTV.setTextColor(mColor);
        holder.uploaderPointsTV.setText(String.valueOf(video.getUploader().getSurgePoints()));

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.thumbnail.setClickable(false);
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


                try {
                    final JSONObject properties = new JSONObject();
                    properties.put("category", mCategory);
                    properties.put("sort", ((MainActivity)mActivity).getSort());
                    mActivity.mMixpanel.track("Video Watched", properties);
                } catch(final JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        holder.downVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Account account = Utils.getAccount(mActivity);
                if(account != null) {
                    API.postVote(mActivity, account, video.getId(), "down", new Response.Listener<Video>() {
                        @Override
                        public void onResponse(Video video) {
                            mVideos.set(position, video);
                            vote(holder.downVoteTV, holder.downVoteIV, video.getDownVote(), true);
                            vote(holder.upVoteTV, holder.upVoteIV, video.getUpVote(), false);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.d(TAG, volleyError.toString());
                        }
                    });
                    animateView(v, new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            Video v = mVideos.get(position);
                            vote(holder.upVoteTV, holder.upVoteIV, v.getUpVote(), false);
                            vote(holder.downVoteTV, holder.downVoteIV, v.getDownVote() + 1, true);
                            return null;
                        }
                    });
                }
            }
        });
        holder.upVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Account account = Utils.getAccount(mActivity);
                if(account != null) {
                    API.postVote(mActivity, account, video.getId(), "up", new Response.Listener<Video>() {
                        @Override
                        public void onResponse(Video newVideo) {
                            Log.d(TAG, "onResponse: " + newVideo.getUpVote() + "  " + System.currentTimeMillis());
                            mVideos.set(position, newVideo);
                            vote(holder.downVoteTV, holder.downVoteIV, newVideo.getDownVote(), false);
                            vote(holder.upVoteTV, holder.upVoteIV, newVideo.getUpVote(), true);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.d(TAG, volleyError.toString());
                        }
                    });
                    animateView(v, new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            Video v = mVideos.get(position);
                            Log.d(TAG, "animation: " + v.getUpVote() + "  " + System.currentTimeMillis());
                            vote(holder.downVoteTV, holder.downVoteIV, v.getDownVote(), false);
                            vote(holder.upVoteTV, holder.upVoteIV, v.getUpVote() + 1, true);
                            return null;
                        }
                    });
                }
            }
        });
        /*holder.userContainerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, ProfileActivity.class);
                intent.putExtra(ProfileActivity.ARG_USER, video.getUploader());
                mActivity.startActivity(intent);
            }
        });*/
    }

    void vote(TextView label, ImageView icon, int count, boolean flag){
        label.setText(String.valueOf(count));
        label.setTextColor(flag ? mColor : mGreyColor);
        Drawable drawable = icon.getDrawable();
        if (flag)
            drawable.setColorFilter(mColor, PorterDuff.Mode.SRC_ATOP);
        else
            drawable.setColorFilter(null);
        icon.setImageDrawable(drawable);

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

        TextView uploaderUsernameTV, uploaderPointsTV;
        TextView downVoteTV, upVoteTV;
        ImageView downVoteIV, upVoteIV;
        View upVote, downVote, overflow, userContainerView;
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
            uploaderUsernameTV = (TextView) itemView.findViewById(R.id.uploader_username);
            uploaderPointsTV = (TextView) itemView.findViewById(R.id.uploader_points);
            userContainerView = itemView.findViewById(R.id.user_container);
        }

    }
}

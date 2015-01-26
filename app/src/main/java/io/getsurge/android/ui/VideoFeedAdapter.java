package io.getsurge.android.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import io.getsurge.android.R;
import io.getsurge.android.model.Video;

/**
 * Created by Gil on 18/01/15.
 */
public class VideoFeedAdapter extends RecyclerView.Adapter<VideoFeedAdapter.VideoViewHolder> {

    private Context mContext;
    private ArrayList<Video> mVideos;

    public VideoFeedAdapter(Context context, ArrayList<Video> videos) {
        mContext = context;
        mVideos = videos;
    }

    @Override
    public VideoFeedAdapter.VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_feed_video, parent, false);
        return new VideoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(VideoFeedAdapter.VideoViewHolder holder, int position) {
        holder.title.setText("Owen Jones vs Jon Gaunt on UKIP");
        holder.thumbnail.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Toast.makeText(mContext, "Video favorited", Toast.LENGTH_SHORT).show();
                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        View upVote, downVote, overflow;
        TextView title;
        ImageView thumbnail;

        public VideoViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.cell_video_feed_title);
            thumbnail = (ImageView) itemView.findViewById(R.id.cell_video_feed_thumbnail);
            upVote = itemView.findViewById(R.id.cell_video_feed_up_vote);
            downVote = itemView.findViewById(R.id.cell_video_feed_down_vote);
            overflow = itemView.findViewById(R.id.cell_video_feed_overflow);
        }

    }
}

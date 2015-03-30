package com.surge.android.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;

/**
 * Created by Gil on 18/01/15.
 */
public class Video {

    String _id;
    String title;
    int up_vote;
    int down_vote;
    String url;
    int category;
    User uploader;

    public String getTitle() {
        return title;
    }

    public int getUpVote() {
        return up_vote;
    }

    public int getDownVote() {
        return down_vote;
    }

    public String getUrl() {
        return url;
    }

    public int getCategory() {
        return category;
    }

    public User getUploader() {
        return uploader;
    }

    public String getId() {
        return _id;
    }
}

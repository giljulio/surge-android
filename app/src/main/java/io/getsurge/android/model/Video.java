package io.getsurge.android.model;

import java.util.Date;

/**
 * Created by Gil on 18/01/15.
 */
public class Video {
    String title;
    int up_vote;
    int down_vote;
    String url;
    int category;

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
}

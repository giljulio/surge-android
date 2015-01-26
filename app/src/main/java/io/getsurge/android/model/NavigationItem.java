package io.getsurge.android.model;

/**
 * Created by poliveira on 24/10/2014.
 */
public class NavigationItem {
    private String mText;

    private int mThumbnail;

    public NavigationItem(String text) {
        mText = text;
    }
    public NavigationItem(String text, int thumbnail){
        mText = text;
        mThumbnail = thumbnail;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public int getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(int mThumbnail) {
        this.mThumbnail = mThumbnail;
    }
}

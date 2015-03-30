package com.surge.android.ui;

import android.support.v7.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Gil on 08/02/15.
 */
public class ObservableOnScrollListener extends RecyclerView.OnScrollListener {

    Set<WeakReference<RecyclerView.OnScrollListener>> mScrollListeners = new LinkedHashSet<>();

    public void addScrollListener(RecyclerView.OnScrollListener onScrollListener){
        WeakReference<RecyclerView.OnScrollListener> weakScrollListener =
                new WeakReference<RecyclerView.OnScrollListener>(onScrollListener);

        mScrollListeners.add(weakScrollListener);
    }

    public void removeScrollListener(RecyclerView.OnScrollListener onScrollListener){
        WeakReference<RecyclerView.OnScrollListener> weakScrollListener =
                new WeakReference<RecyclerView.OnScrollListener>(onScrollListener);

        mScrollListeners.remove(weakScrollListener);
    }


    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        for (WeakReference<RecyclerView.OnScrollListener> weakListener : mScrollListeners){
            weakListener.get().onScrollStateChanged(recyclerView, newState);
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        for (WeakReference<RecyclerView.OnScrollListener> weakListener : mScrollListeners){
            weakListener.get().onScrolled(recyclerView, dx, dy);
        }
    }
}

package com.surge.android.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.lang.ref.WeakReference;

/**
* Created by Gil on 08/02/15.
*/
public class HideExtraOnScroll extends RecyclerView.OnScrollListener{

    final static Interpolator ACCELERATE = new AccelerateInterpolator();
    final static Interpolator DECELERATE = new DecelerateInterpolator();

    static HideExtraOnScroll mInstance;
    static HideExtraOnScroll mFABInstance;

    WeakReference<View> mTarget;
    HideExtraOnScrollHelper mScrollHelper;

    boolean isExtraObjectsOutside;

    boolean mAnimateViewDown = false;

    public HideExtraOnScroll(View target) {
        int minimumFlingVelocity = ViewConfiguration.get(target.getContext())
                .getScaledMinimumFlingVelocity();

        mScrollHelper = new HideExtraOnScrollHelper(minimumFlingVelocity);
        mTarget = new WeakReference<View>(target);

    }


    public static HideExtraOnScroll getInstance(View target){
        if(mInstance == null){
            mInstance = new HideExtraOnScroll(target);
        }
        if (!target.equals(mInstance.mTarget.get())){
            mInstance.setTarget(target);
        }
        return mInstance;
    }

    public static HideExtraOnScroll getFABInstance(View target){
        if(mFABInstance == null){
            mFABInstance = new HideExtraOnScroll(target);
            mFABInstance.mAnimateViewDown = true;
        }
        if (!target.equals(mFABInstance.mTarget.get())){
            mFABInstance.setTarget(target);
        }
        return mFABInstance;
    }

    public void setTarget(View mTarget) {
        this.mTarget = new WeakReference<View>(mTarget);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        final View target = mTarget.get();

        if(target == null) {
            return;
        }

        boolean isObjectsShouldBeOutside = mScrollHelper.isObjectsShouldBeOutside(dy);

        if(!isVisible(target) && !isObjectsShouldBeOutside){
            show(target);
            isExtraObjectsOutside = false;
        }else if(isVisible(target) && isObjectsShouldBeOutside){
            hide(target, mAnimateViewDown ? target.getHeight() : -target.getHeight());
            isExtraObjectsOutside = true;
        }
    }

    public boolean isVisible(View target){
        return !isExtraObjectsOutside;
    }

    public void hide(final View target, float distance){
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationY",
                ViewHelper.getTranslationY(target), distance);
        animator.setInterpolator(DECELERATE);
        animator.start();
    }

    public void show(final View target){
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationY",
                ViewHelper.getTranslationY(target), 0f);
        animator.setInterpolator(ACCELERATE);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
//                target.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }


    public static class HideExtraOnScrollHelper{
        public final static int UNKNOWN = -1;
        public final static int TOP = 0;
        public final static int BOTTOM = 1;

        int mDraggedAmount;
        int mOldDirection;
        int mDragDirection;

        final int mMinFlingDistance;

        public HideExtraOnScrollHelper(int minFlingDistance) {
            mOldDirection  =
                    mDragDirection =
                            mDraggedAmount = UNKNOWN;

            mMinFlingDistance = minFlingDistance;
        }

        /**
         * Checks need to hide extra objects on scroll or not
         *
         * @param dy scrolled distance y
         * @return true if need to hide extra objects on screen
         */
        public boolean isObjectsShouldBeOutside(int dy){
            boolean needHide = false;
            mDragDirection = dy > 0 ? BOTTOM : TOP;

            if(mDragDirection != mOldDirection){
                mDraggedAmount = 0;
            }

            mDraggedAmount += dy;
            boolean shouldBeOutside = false;

            if(mDragDirection == TOP && Math.abs(mDraggedAmount) > mMinFlingDistance){
                shouldBeOutside = false;
            }else if(mDragDirection == BOTTOM && mDraggedAmount > mMinFlingDistance){
                shouldBeOutside = true;
            }

            if(mOldDirection != mDragDirection){
                mOldDirection = mDragDirection;
            }

            return shouldBeOutside;
        }
    }

}

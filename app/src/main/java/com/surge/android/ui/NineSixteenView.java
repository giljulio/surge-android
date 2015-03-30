package com.surge.android.ui;

import android.content.Context;
import android.util.AttributeSet;


/**
 * Created by Gil on 18/01/15.
 */
public class NineSixteenView extends FadeInNetworkImageView {

    private static final double RATIO = 0.56;

    public NineSixteenView(Context context) {
        super(context);
    }

    public NineSixteenView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NineSixteenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, (int) (width * RATIO));
    }
}

package io.getsurge.android.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Gil on 18/01/15.
 */
public class NineSixteenView extends ImageView {

    private static final double RATIO = 0.55;

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

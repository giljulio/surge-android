package io.getsurge.android.ui;

import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import io.getsurge.android.R;
import io.getsurge.android.utils.Utils;

/**
 * Created by Gil on 16/01/15.
 */
public class SurgeView extends View {

    private static final int COLOUM_COUNT = 6;

    final RectF mRects[] = new RectF[COLOUM_COUNT];
    Paint mPaints[] = new Paint[COLOUM_COUNT];

    int mWidth;
    int mHeight;


    public SurgeView(Context context) {
        super(context);
    }

    public SurgeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SurgeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.mWidth = w;
        this.mHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);
        mPaints[0] = new Paint();
        mPaints[0].setColor(getResources().getColor(R.color.category_color_wow));
        mPaints[1] = new Paint();
        mPaints[1].setColor(getResources().getColor(android.R.color.holo_red_light));
        mPaints[1] = new Paint();
        mPaints[1].setColor(getResources().getColor(android.R.color.holo_purple));
        mPaints[2] = new Paint();
        mPaints[2].setColor(getResources().getColor(R.color.category_color_all));
        mPaints[3] = new Paint();
        mPaints[3].setColor(getResources().getColor(R.color.category_color_comedy));
        mPaints[4] = new Paint();
        mPaints[4].setColor(getResources().getColor(R.color.category_color_animals));
        mPaints[5] = new Paint();
        mPaints[5].setColor(getResources().getColor(R.color.category_color_other));
        init();
    }

    public void init(){
        float colWidth = (float) Math.sqrt(mWidth * mWidth + mHeight * mHeight) / COLOUM_COUNT;
        //float colWidth = mWidth/COLOUM_COUNT > mHeight/COLOUM_COUNT ? mWidth/COLOUM_COUNT : mHeight/COLOUM_COUNT;
        for (int i = 0; i < COLOUM_COUNT; i++) {
            final RectF finalizedRect = mRects[i] = new RectF(colWidth * i, -mHeight * 1.5F, colWidth * (i + 1), -mHeight * 1.5F);
            ValueAnimator valueAnimator = new ValueAnimator();
            valueAnimator.setDuration(300L);
            valueAnimator.setStartDelay(Utils.random(0, 600));
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.setEvaluator(new FloatEvaluator());
            valueAnimator.setObjectValues(-mHeight, mHeight);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    finalizedRect.bottom = (int) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            valueAnimator.start();
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mWidth == 0 || mHeight == 0) {
            mWidth = canvas.getWidth();
            mHeight = canvas.getWidth();
        }

        canvas.save();

        canvas.rotate(45, 0, 0);

        for(int i = 0; i < COLOUM_COUNT; i++)
            canvas.drawRect(mRects[i], mPaints[i]);

        canvas.restore();
    }
}

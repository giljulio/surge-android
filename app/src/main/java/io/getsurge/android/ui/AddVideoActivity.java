package io.getsurge.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import io.getsurge.android.R;

public class AddVideoActivity extends BaseActivity {

    public static final String KEY_REVEAL_X_COORD = "x_coord";
    public static final String KEY_REVEAL_Y_COORD = "y_coord";

    private int mRevealX, mRevealY;

    @InjectView(R.id.activity_container)
    View mContainer;

    @InjectView(R.id.select_category_container)
    View mSelectCategoryContainer;

    @InjectView(R.id.select_category_label)
    TextView mSelectCategoryLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);
        ButterKnife.inject(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Post a cheeky video");



        Intent intent = getIntent();
        mRevealX = intent.getIntExtra(KEY_REVEAL_X_COORD, 0);
        mRevealY = intent.getIntExtra(KEY_REVEAL_Y_COORD, 0);

        mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                View myView = mContainer;

                // get the final radius for the clipping circle
                float finalRadius = hypo(myView.getWidth(), myView.getHeight());

                SupportAnimator animator =
                        ViewAnimationUtils.createCircularReveal(myView, mRevealX, mRevealY, 0, finalRadius);
                animator.setInterpolator(new AccelerateInterpolator());
                animator.setDuration(400);
                animator.start();
            }
        });
        mSelectCategoryContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(AddVideoActivity.this)
                        .title("Select a category")
                        .items(R.array.categories)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                mSelectCategoryLabel.setText(text);
                            }
                        }).show();
            }
        });
        mSelectCategoryLabel.setText("Selected one");
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.scale_up, R.anim.slide_down);
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.scale_up, R.anim.slide_down);
    }

    float hypo(int a, int b){
        return (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

}
package com.surge.android.ui;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import com.surge.android.R;
import com.surge.android.model.BasicVideo;
import com.surge.android.model.GenericError;
import com.surge.android.model.Video;
import com.surge.android.net.API;
import com.surge.android.utils.Utils;

public class AddVideoActivity extends BaseActivity implements TextWatcher, Response.Listener<BasicVideo>, Response.ErrorListener {

    public static final String KEY_REVEAL_X_COORD = "x_coord";
    public static final String KEY_REVEAL_Y_COORD = "y_coord";

    private static final String TAG = AddVideoActivity.class.getSimpleName();

    private static final Gson gson = new Gson();

    private int mRevealX, mRevealY;

    @InjectView(R.id.activity_container)
    View mContainer;

    @InjectView(R.id.select_category_container)
    View mSelectCategoryContainer;

    @InjectView(R.id.select_category_label)
    TextView mSelectCategoryLabel;

    @InjectView(R.id.add_video_title)
    EditText mTitle;

    @InjectView(R.id.add_video_url)
    EditText mUrl;

    @InjectView(R.id.add_video_submit)
    TextView mSubmit;

    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);
        ButterKnife.inject(this);

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        String action = i.getAction();

        for (String key : extras.keySet()) {
            Object value = extras.get(key);
            Log.d(TAG, String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));
        }

        if (Intent.ACTION_SEND.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_TEXT)) {
                String text = i.getStringExtra(Intent.EXTRA_TEXT);

                Pattern urlPattern = Pattern.compile(
                        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

                Matcher matcher = urlPattern.matcher(text);
                if (matcher.find()) {
                    String url = text.substring(matcher.start(1), matcher.end());
                    mUrl.setText(url);
                } else {
                    Crouton.makeText(this, "Failed to get URL of the video. Please get the URL manually. ", Style.ALERT).show();
                }
            }
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



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
                        .items(R.array.categories)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                mSelectCategoryContainer.setTag(which);
                                mSelectCategoryLabel.setText(text);
                                updateView();
                            }
                        }).show();
            }
        });
        mSelectCategoryContainer.setTag(-1);
        mSelectCategoryLabel.setText("None selected");
        updateView();
        mTitle.addTextChangedListener(this);
        mUrl.addTextChangedListener(this);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account account = Utils.getAccount(AddVideoActivity.this);
                if(account != null){
                    mProgress = new ProgressDialog(AddVideoActivity.this);
                    mProgress.setMessage("Posting video...");
                    mProgress.show();
                    API.postVideo(AddVideoActivity.this,
                            account,
                            mTitle.getText().toString(),
                            mUrl.getText().toString(),
                            mSelectCategoryContainer.getTag().toString(),
                            AddVideoActivity.this, AddVideoActivity.this);
                }
            }
        });
    }

    void updateView(){
        boolean valid = true;
        if((int) mSelectCategoryContainer.getTag() == -1){
            valid = false;
        }

        if(mTitle.getText().toString().isEmpty()){
            valid = false;
        }

        if(mUrl.getText().toString().isEmpty()){
            valid = false;
        }

        mSubmit.setEnabled(valid);
        mSubmit.setAlpha(valid ? 1 : 0.6F);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                                    // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        updateView();
    }

    @Override
    public void onResponse(BasicVideo video) {
        if (mProgress != null)
            mProgress.hide();
        finish();
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        if(volleyError != null) {
            Log.d(TAG, "volley error: " +  volleyError.toString());
            if(volleyError.networkResponse != null) {
                Log.d(TAG, "Status code: " + String.valueOf(volleyError.networkResponse.statusCode));
            }
        }
        if (mProgress != null)
            mProgress.hide();

        try {
            String json = new String(
                    volleyError.networkResponse.data,
                    HttpHeaderParser.parseCharset(volleyError.networkResponse.headers));
            GenericError error = gson.fromJson(json, GenericError.class);
            if(error.getType().equals("invalid-video-title")){
                mTitle.setError("The title needs to be at least six characters");
                mTitle.requestFocus();
            } else {
                mTitle.setError(null);
            }

            if(error.getType().equals("unsupported-site-url")){
                mUrl.setError("The link provided is from a site that is not yet supported");
                mUrl.requestFocus();
            } else if(error.getType().equals("invalid-video-url")){
                mUrl.setError("The link provided is not valid");
                mUrl.requestFocus();
            } else {
                mUrl.setError(null);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occurred.", Toast.LENGTH_SHORT).show();
        }


    }
}
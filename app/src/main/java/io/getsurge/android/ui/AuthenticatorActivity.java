package io.getsurge.android.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.getsurge.android.Constants;
import io.getsurge.android.R;
import io.getsurge.android.model.AuthenticateResponse;
import io.getsurge.android.model.GenericError;
import io.getsurge.android.net.API;

/**
 * A login screen that offers login via email/password.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity implements Response.Listener<AuthenticateResponse>, Response.ErrorListener {

    /** The tag used to log to adb console. */
    private static final String TAG = AuthenticatorActivity.class.getSimpleName();

    private AccountManager mAccountManager;

    /** The Intent flag to confirm credentials. */
    public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";

    /** The Intent extra to store password. */
    public static final String PARAM_PASSWORD = "password";

    /** The Intent extra to store username. */
    public static final String PARAM_EMAIL = "username";

    /** The Intent extra to store username. */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    private Boolean mConfirmCredentials = false;

    /** Was the original caller asking for an entirely new account? */
    protected boolean mRequestNewAccount = false;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
//    private UserLoginTask mAuthTask = null;

    // UI references.
    @InjectView(R.id.signin_user)
    EditText mSignInUserView;

    @InjectView(R.id.signin_password)
    EditText mSignInPasswordView;

    @InjectView(R.id.signup_email)
    EditText mSignUpEmailView;

    @InjectView(R.id.signup_username)
    EditText mSignUpUsernameView;

    @InjectView(R.id.signup_password)
    EditText mSignUpPasswordView;

    @InjectView(R.id.login_progress)
    View mProgressView;

    @InjectView(R.id.login_form)
    View mLoginFormView;

    @InjectView(R.id.submit)
    Button mSubmit;

    private boolean isSigningUp;

    @InjectView(R.id.is_registered)
    TextView mIsRegisteredButton;

    @InjectView(R.id.container_signin)
    View mSignInContainer;

    @InjectView(R.id.container_signup)
    View mSignUpContainer;

    Gson gson = new Gson();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountManager = AccountManager.get(this);
        setContentView(R.layout.activity_login2);
        ButterKnife.inject(this);

        mIsRegisteredButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isSigningUp = !isSigningUp;
                updateType();
            }
        });

        updateType();

        mSignInPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        final Intent intent = getIntent();
        if(intent.getStringExtra(PARAM_EMAIL) != null) {
            mSignInUserView.setText(intent.getStringExtra(PARAM_EMAIL));
        } else {
            mRequestNewAccount = true;
        }
        mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRM_CREDENTIALS, false);
    }


    private void updateType(){
        mActionBarToolbar.setTitle(!isSigningUp ? R.string.action_sign_in_short : R.string.action_register_short);
        mSubmit.setText(!isSigningUp ? R.string.action_sign_in_short : R.string.action_register_short);
        mIsRegisteredButton.setText(isSigningUp ? R.string.action_sign_in_have_an_account : R.string.action_register_an_account);
        mSignInContainer.setVisibility(isSigningUp ? View.GONE : View.VISIBLE);
        mSignUpContainer.setVisibility(!isSigningUp ? View.GONE : View.VISIBLE);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        /*if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mSignInUserView.setError(null);
        mSignInPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mSignInUserView.getText().toString();
        String password = mSignInPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mSignInPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mSignInPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mSignInUserView.setError(getString(R.string.error_field_required));
            focusView = mSignInUserView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mSignInUserView.setError(getString(R.string.error_invalid_email));
            focusView = mSignInUserView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }*/
        showProgress(true);
        if(isSigningUp){
            API.attemptSignUp(this,
                    mSignUpUsernameView.getText().toString(),
                    mSignUpEmailView.getText().toString(),
                    mSignUpPasswordView.getText().toString(),
                    this, this);
        } else {
            API.attemptSignIn(this,
                    mSignInUserView.getText().toString(),
                    mSignInPasswordView.getText().toString(),
                    this, this);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onResponse(AuthenticateResponse response) {
        showProgress(false);
        if (!mConfirmCredentials) {
            finishLogin(response);
        } else {
            finishConfirmCredentials(true);
        }

    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        Log.e(TAG, volleyError.toString());
        Log.d(TAG, "Network response is : " + String.valueOf(volleyError.networkResponse != null));
        showProgress(false);
        try {
            String json = new String(
                    volleyError.networkResponse.data,
                    HttpHeaderParser.parseCharset(volleyError.networkResponse.headers));
            Log.e(TAG, json);
            GenericError error = gson.fromJson(json, GenericError.class);

            if(isSigningUp) {
                if (error.getType().equals("username-exists")) {
                    mSignUpUsernameView.setError("Username already exists, please try another.");
                    mSignUpUsernameView.requestFocus();
                } else if (error.getType().equals("invalid-password")){
                    mSignUpPasswordView.setError("Password must have at least 8 characters.");
                    mSignUpPasswordView.requestFocus();
                } else if (error.getType().equals("invalid-email")){
                    mSignUpEmailView.setError("Email is invalid.");
                    mSignUpEmailView.requestFocus();
                } else if (error.getType().equals("invalid-username")){
                    mSignUpUsernameView.setError("Invalid username (Tip: You cannot have any spaces)");
                    mSignUpUsernameView.requestFocus();
                } else if (error.getType().equals("email-exists")){
                    mSignUpEmailView.setError("Email already exists");
                    mSignUpEmailView.requestFocus();
                } else if (error.getType().equals("username-exists")){
                    mSignUpUsernameView.setError("Username already exists");
                    mSignUpUsernameView.requestFocus();
                }
            } else {
                if (error.getType().equals("incorrect-credentials")) {
                    // "Please enter a valid username/password.
                    mSignInUserView.setError(getString(R.string.login_activity_loginfail_text_both));
                    mSignInUserView.requestFocus();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Couldn't get a response from the server", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    /*public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected String doInBackground(Void... params) {
            return API.getAuthToken(AuthenticatorActivity.this, mEmail, mPassword);
        }

        @Override
        protected void onPostExecute(final String authToken) {
            mAuthTask = null;
            showProgress(false);
            boolean success = authToken != null && !authToken.isEmpty();
            if (success) {
                if (!mConfirmCredentials) {
                    finishLogin(authToken);
                } else {
                    finishConfirmCredentials(success);
                }
            } else {
                Log.e(TAG, "onAuthenticationResult: failed to authenticate");
                if (mRequestNewAccount) {
                    // "Please enter a valid username/password.
                    mSignInUserView.setError(getString(R.string.login_activity_loginfail_text_both));
                    mSignInUserView.requestFocus();
                } else {
                    // "Please enter a valid password." (Used when the
                    // account is already in the database but the password
                    // doesn't work.)
                    mSignInPasswordView.setError(getString(R.string.error_incorrect_password));
                    mSignInPasswordView.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }*/


    /**
     * Called when response is received from the server for confirm credentials
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller.
     *
     * @param result the confirmCredentials result.
     */
    private void finishConfirmCredentials(boolean result) {
        Log.i(TAG, "finishConfirmCredentials()");
        final Account account = new Account(mSignInUserView.getText().toString(), Constants.ACCOUNT_TYPE);
        mAccountManager.setPassword(account, mSignInPasswordView.getText().toString());
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Called when response is received from the server for authentication
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller. We store the
     * authToken that's returned from the server as the 'password' for this
     * account - so we're never storing the user's actual password locally.
     *
     * @param response from the server.
     */
    private void finishLogin(AuthenticateResponse response) {
        Log.v(TAG, "finishLogin()");
        String email = response.getUser().getEmail();
        String password = isSigningUp ?
                mSignUpPasswordView.getText().toString() : mSignInPasswordView.getText().toString();
        final Account account = new Account("@" + response.getUser().getUsername(), Constants.ACCOUNT_TYPE);
        if (mRequestNewAccount) {
            Log.v(TAG, "New account");
            mAccountManager.addAccountExplicitly(account, password, null);
            mAccountManager.setAuthToken(account, Constants.AUTHTOKEN_TYPE, response.getToken());
            //TODO(gil): this?
            //ContentResolver.setIsSyncable(account, ReceiptsContract.CONTENT_AUTHORITY, 1);
            // Set contacts sync for this account.
            //ContentResolver.setSyncAutomatically(account, ReceiptsContract.CONTENT_AUTHORITY, true);
        } else {
            mAccountManager.setPassword(account, password);
            mAccountManager.setAuthToken(account, Constants.AUTHTOKEN_TYPE, response.getToken());
        }
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, email);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.scale_up, R.anim.slide_down);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.scale_up, R.anim.slide_down);

    }
}




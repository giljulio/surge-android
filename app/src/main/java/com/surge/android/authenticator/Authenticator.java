/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.surge.android.authenticator;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.surge.android.Constants;
import com.surge.android.R;
import com.surge.android.net.API;
import com.surge.android.ui.AuthenticatorActivity;

import static com.surge.android.utils.LogUtils.LOGV;
import static com.surge.android.utils.LogUtils.makeLogTag;
import static com.surge.android.utils.LogUtils.LOGD;

/**
 * This class is an implementation of AbstractAccountAuthenticator for
 * authenticating accounts in the com.example.android.samplesync domain. The
 * interesting thing that this class demonstrates is the use of authTokens as
 * part of the authentication process. In the account setup UI, the user enters
 * their username and password. But for our subsequent calls off to the service
 * for syncing, we want to use an authtoken instead - so we're not continually
 * sending the password over the wire. getAuthToken() will be called when
 * SyncAdapter calls AccountManager.blockingGetAuthToken(). When we get called,
 * we need to return the appropriate authToken for the specified account. If we
 * already have an authToken stored in the account, we return that authToken. If
 * we don't, but we do have a username and password, then we'll attempt to talk
 * to the sample service to fetch an authToken. If that fails (or we didn't have
 * a username/password), then we need to prompt the user - so we create an
 * AuthenticatorActivity intent and return that. That will display the dialog
 * that prompts the user for their login information.
 */
class Authenticator extends AbstractAccountAuthenticator {

    private static final String TAG = makeLogTag(Authenticator.class);

    public static final int ERROR_CODE_ONE_ACCOUNT_ALLOWED = 32;

    // Authentication Service context
    private final Context mContext;

    private final Handler handler = new Handler();

    public Authenticator(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
            String authTokenType, String[] requiredFeatures, Bundle options) {
        LOGD(TAG, "addAccount()");


        if (AccountManager.get(mContext).getAccountsByType(accountType).length > 0) {
            final Bundle result = new Bundle();
            result.putInt(AccountManager.KEY_ERROR_CODE, ERROR_CODE_ONE_ACCOUNT_ALLOWED);
            result.putString(AccountManager.KEY_ERROR_MESSAGE, mContext.getString(R.string.one_account_allowed));

            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(mContext, R.string.one_account_allowed, Toast.LENGTH_LONG).show();
                }
            });
            return result;
        }

        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(
            AccountAuthenticatorResponse response, Account account, Bundle options) {
        LOGD(TAG, "confirmCredentials()");
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        LOGD(TAG, "editProperties()");
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
            String authTokenType, Bundle loginOptions) throws NetworkErrorException {
        LOGD(TAG, "getAuthToken()");


        // If the caller requested an authToken type we don't support, then
        // return an error
        if (!authTokenType.equals(mContext.getString(R.string.token_type))) {
            LOGV(TAG, "Auth token type doesn't match. " + authTokenType + " - " + mContext.getString(R.string.token_type));
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }

        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        final AccountManager am = AccountManager.get(mContext);
        final String password = am.getPassword(account);
        if (password != null) {
            LOGV(TAG, "Password is set. Checking if it's valid.");
            final String authToken = API.getAuthToken(mContext, account.name, password);
            if (authToken != null && !TextUtils.isEmpty(authToken)) {
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, mContext.getString(R.string.account_type));
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                return result;
            }
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity panel.
        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AuthenticatorActivity.PARAM_EMAIL, account.name);
        intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        // null means we don't support multiple authToken types
        LOGV(TAG, "getAuthTokenLabel()");
        return null;
    }

    @Override
    public Bundle hasFeatures(
            AccountAuthenticatorResponse response, Account account, String[] features) {
        // This call is used to query whether the Authenticator supports
        // specific features. We don't expect to get called, so we always
        // return false (no) for any queries.
        Log.v(TAG, "hasFeatures()");
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
            String authTokenType, Bundle loginOptions) {
        Log.v(TAG, "updateCredentials()");
        return null;
    }

    @Override @NonNull
    public Bundle getAccountRemovalAllowed(
            AccountAuthenticatorResponse response, Account account)
            throws NetworkErrorException {
        Bundle result = super.getAccountRemovalAllowed(response, account);

        if (result != null && result.containsKey(AccountManager.KEY_BOOLEAN_RESULT)
                && !result.containsKey(AccountManager.KEY_INTENT)) {
            final boolean removalAllowed = result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT);

            if (removalAllowed) {
                MixpanelAPI.getInstance(mContext, Constants.MIX_PANEL).reset();
            }
        }

        return result;
    }

}

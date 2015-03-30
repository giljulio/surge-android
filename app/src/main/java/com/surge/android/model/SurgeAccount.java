package com.surge.android.model;

import android.accounts.Account;

/**
 * Created by Gil on 21/02/15.
 */
public class SurgeAccount {

    Account account;
    String token;

    public SurgeAccount(Account account, String token) {
        this.account = account;
        this.token = token;
    }

    public Account getAccount() {
        return account;
    }

    public String getToken() {
        return token;
    }
}

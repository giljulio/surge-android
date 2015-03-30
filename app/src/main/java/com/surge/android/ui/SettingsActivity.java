package com.surge.android.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

import com.surge.android.BuildConfig;
import com.surge.android.Constants;
import com.surge.android.R;

import java.util.Random;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.GnuLesserGeneralPublicLicense21;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SurgePreferenceFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            overridePendingTransition(R.anim.scale_up, R.anim.slide_down);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.scale_up, R.anim.slide_down);
    }

    public static class SurgePreferenceFragment extends PreferenceFragment implements OnAccountsUpdateListener {

        Preference mAccountContainer;
        Preference mUsername;
        Preference mSupportHelp;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_general);
            mAccountContainer = findPreference("account");
            mUsername = findPreference("username");
            mSupportHelp = findPreference("support");


            getPreferenceScreen().removePreference(mAccountContainer);

        }

        @Override
        public void onResume() {
            super.onResume();
            AccountManager.get(getActivity()).addOnAccountsUpdatedListener(this, null, true);
        }

        @Override
        public void onStop() {
            super.onStop();
            AccountManager.get(getActivity()).removeOnAccountsUpdatedListener(this);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if(preference != null && preference.hasKey()){
                if(preference.getKey().equals("logout")){
                    final Account[] accounts = AccountManager.get(getActivity()).getAccountsByType(getString(R.string.account_type));
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Are you sure you want to logout?")
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AccountManager.get(getActivity()).removeAccount(accounts[0], null, null);
                                }
                            })
                            .show();
                    return true;
                } else if (preference.getKey().equals("support")){
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "support@trysurge.com", null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Ticket #" + new Random().nextInt(100000));
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Android Version: " + Build.VERSION.SDK_INT + "\n" +
                            "App Version: " + BuildConfig.VERSION_NAME + "\n");
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                } else if (preference.getKey().equals("rate_us")){
                    final String appPackageName = getActivity().getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                } else if (preference.getKey().equals("open_source")){
                    final Notices notices = new Notices();
                    notices.addNotice(new Notice("Butterknife", "http://jakewharton.github.io/butterknife", "Jake Wharton", new ApacheSoftwareLicense20()));
                    notices.addNotice(new Notice("Youtube SDK", "https://developers.google.com/youtube/android/player/", "Google Inc", new ApacheSoftwareLicense20()));
                    notices.addNotice(new Notice("Gson", "https://code.google.com/p/google-gson", "Google Inc", new ApacheSoftwareLicense20()));
                    notices.addNotice(new Notice("Volley", "https://android.googlesource.com/platform/frameworks/volley", "The Android Open Source Project", new ApacheSoftwareLicense20()));
                    notices.addNotice(new Notice("Floating Action Button", "https://github.com/makovkastar/FloatingActionButton", "Oleksandr Melnykov", new MITLicense()));
                    notices.addNotice(new Notice("Licenses Dialog", "https://github.com/PSDev/LicensesDialog", "Philip Schiffer", new ApacheSoftwareLicense20()));

                    new LicensesDialog.Builder(getActivity()).setNotices(notices).setIncludeOwnLicense(true).build().show();
                } else if (preference.getKey().equals("terms")){
                    String url = "http://www.trysurge.com/terms";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } else if (preference.getKey().equals("privacy")){
                    String url = "http://www.trysurge.com/privacy";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            }
            return false;
        }

        @Override
        public void onAccountsUpdated(Account[] accounts) {

            accounts = AccountManager.get(getActivity()).getAccountsByType(getString(R.string.account_type));
            if(accounts.length > 0) {
                getPreferenceScreen().addPreference(mAccountContainer);
                Account account = accounts[0];
                mUsername.setSummary("@" + account.name);
            } else {
                getPreferenceScreen().removePreference(mAccountContainer);
            }
        }
    }

}

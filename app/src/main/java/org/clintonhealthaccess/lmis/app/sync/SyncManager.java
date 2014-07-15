package org.clintonhealthaccess.lmis.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.User;

import roboguice.inject.InjectResource;

import static android.content.ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY;
import static android.content.ContentResolver.SYNC_EXTRAS_EXPEDITED;
import static android.content.ContentResolver.SYNC_EXTRAS_MANUAL;
import static android.content.ContentResolver.addPeriodicSync;
import static android.content.ContentResolver.setIsSyncable;
import static android.content.ContentResolver.setSyncAutomatically;
import static android.content.Context.ACCOUNT_SERVICE;
import static android.util.Log.i;
import static java.lang.String.valueOf;

public class SyncManager {
    @Inject
    private Context context;

    @InjectResource(R.string.sync_content_authority)
    private String syncContentAuthority;

    @InjectResource(R.string.sync_account_type)
    private String syncAccountType;

    public void kickOff() {
        Account[] accounts = getAccountManager().getAccounts();
        i("###### amount of accounts : ", valueOf(accounts.length));
        if (accounts.length > 0) {
            Account account = accounts[0];
            setIsSyncable(account, syncContentAuthority, 1);
            setSyncAutomatically(account, syncContentAuthority, true);

            Bundle extras = new Bundle();
            extras.putBoolean(SYNC_EXTRAS_DO_NOT_RETRY, false);
            extras.putBoolean(SYNC_EXTRAS_EXPEDITED, false);
            extras.putBoolean(SYNC_EXTRAS_DO_NOT_RETRY, false);
            extras.putBoolean(SYNC_EXTRAS_MANUAL, false);
            addPeriodicSync(account, syncContentAuthority, extras, 60);

            i("==> auto sync enabled to : ", account.name);
        }
    }

    public void createSyncAccount(User user) {
        Account account = new Account(user.getUsername(), syncAccountType);
        boolean success = getAccountManager().addAccountExplicitly(account, user.getPassword(), null);
        i("==> Create Sync Account : ", valueOf(success));
    }

    private AccountManager getAccountManager() {
        return (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
    }
}

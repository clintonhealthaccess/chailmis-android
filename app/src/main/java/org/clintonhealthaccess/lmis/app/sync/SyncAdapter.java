package org.clintonhealthaccess.lmis.app.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.persistence.DbUtil;

import roboguice.RoboGuice;

import static android.util.Log.i;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private final ContentResolver contentResolver;

    @Inject
    DbUtil dbUtil;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        contentResolver = context.getContentResolver();
        RoboGuice.getInjector(context).injectMembers(this);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        i("==> Syncing...........", account.name);


    }
}

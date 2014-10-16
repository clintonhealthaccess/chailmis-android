/*
 * Copyright (c) 2014, Thoughtworks Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package org.clintonhealthaccess.lmis.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;

import com.google.common.base.Predicate;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.User;

import java.util.List;

import roboguice.inject.InjectResource;

import static android.content.ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY;
import static android.content.ContentResolver.SYNC_EXTRAS_EXPEDITED;
import static android.content.ContentResolver.SYNC_EXTRAS_MANUAL;
import static android.content.ContentResolver.addPeriodicSync;
import static android.content.ContentResolver.setIsSyncable;
import static android.content.ContentResolver.setSyncAutomatically;
import static android.util.Log.i;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.valueOf;

public class SyncManager {
    @Inject
    private AccountManager accountManager;

    @InjectResource(R.string.sync_content_authority)
    private String syncContentAuthority;

    @InjectResource(R.string.sync_account_type)
    private String syncAccountType;

    @InjectResource(R.integer.sync_interval)
    private Integer syncInterval;

    public void kickOff() {
        List<Account> accounts = newArrayList(accountManager.getAccounts());
        i("###### amount of accounts : ", valueOf(accounts.size()));

        List<Account> lmisAccounts = from(accounts).filter(new Predicate<Account>() {
            @Override
            public boolean apply(Account input) {
                return syncAccountType.equals(input.type);
            }
        }).toList();

        if (lmisAccounts.size() > 0) {
            kickOffFor(lmisAccounts.get(0));
        }
    }

    private void kickOffFor(Account account) {
        setIsSyncable(account, syncContentAuthority, 1);
        setSyncAutomatically(account, syncContentAuthority, true);
        addPeriodicSync(account, syncContentAuthority, periodicSyncParams(), syncInterval);
        i("==> auto sync enabled to : ", account.name);
    }

    private Bundle periodicSyncParams() {
        Bundle extras = new Bundle();
        extras.putBoolean(SYNC_EXTRAS_DO_NOT_RETRY, false);
        extras.putBoolean(SYNC_EXTRAS_EXPEDITED, false);
        extras.putBoolean(SYNC_EXTRAS_DO_NOT_RETRY, false);
        extras.putBoolean(SYNC_EXTRAS_MANUAL, false);
        return extras;
    }

    public void createSyncAccount(User user) {
        Account account = new Account(user.getUsername(), syncAccountType);
        boolean success = accountManager.addAccountExplicitly(account, user.getPassword(), null);
        i("==> Create Sync Account : ", valueOf(success));
    }

}

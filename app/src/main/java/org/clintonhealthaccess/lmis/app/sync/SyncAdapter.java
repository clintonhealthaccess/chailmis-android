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
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.services.AllocationService;
import org.clintonhealthaccess.lmis.app.services.CommodityActionService;
import org.clintonhealthaccess.lmis.app.services.CommodityService;
import org.clintonhealthaccess.lmis.app.services.CommoditySnapshotService;
import org.clintonhealthaccess.lmis.app.services.UserService;

import roboguice.RoboGuice;

import static android.util.Log.i;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private final ContentResolver contentResolver;

    @Inject
    DbUtil dbUtil;
    @Inject
    CommoditySnapshotService commoditySnapshotService;
    @Inject
    AllocationService allocationService;
    @Inject
    CommodityActionService commodityActionService;
    @Inject
    CommodityService commodityService;

    @Inject
    UserService userService;


    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        contentResolver = context.getContentResolver();
        RoboGuice.getInjector(context).injectMembers(this);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        User user = userService.getRegisteredUser();
        i("==> Syncing...........SNAPSHOTS", account.name);
        commoditySnapshotService.syncWithServer(user);

        i("<== Syncing............ALLOCATIONS", account.name);
        allocationService.syncAllocations(user);

        i("<== Syncing............ACTIONVALUES", account.name);
        commodityActionService.syncCommodityActionValues(user);

    }
}

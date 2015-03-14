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

package org.clintonhealthaccess.lmis.app.models.alerts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.clintonhealthaccess.lmis.app.activities.ReceiveActivity;
import org.clintonhealthaccess.lmis.app.models.Allocation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@DatabaseTable
public class AllocationAlert implements NotificationMessage {
    public static final String DATE_CREATED = "dateCreated";
    public static final String ALLOCATION_ID_COLUMN = "allocation_id";
    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = true, columnName = DATE_CREATED)
    private Date dateCreated;

    @DatabaseField(canBeNull = false, foreignAutoRefresh = true, foreign = true, columnName = ALLOCATION_ID_COLUMN)
    private Allocation allocation;

    public AllocationAlert() {
        //OrmLite Likes
    }

    public AllocationAlert(Allocation allocation) {
        this.allocation = allocation;
        this.dateCreated = new Date();
    }

    @Override
    public String getMessage() {
        return "New Allocation " + allocation.getAllocationId() + " created on " + allocation.getPeriod();
    }

    @Override
    public void onClick(Context context) {
        Log.i("Allocation alert clicked", getMessage());
        Intent intent = new Intent(context, ReceiveActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle data = new Bundle();
        data.putString(ReceiveActivity.ALLOCATION_ID, allocation.getAllocationId());
        intent.putExtras(data);
        context.startActivity(intent);

    }

    public Allocation getAllocation() {
        return allocation;
    }
}

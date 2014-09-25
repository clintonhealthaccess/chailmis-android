/*
 * Copyright (c) 2014, ThoughtWorks
 *
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

import org.clintonhealthaccess.lmis.app.activities.OrderActivity;
import org.clintonhealthaccess.lmis.app.listeners.AlertClickListener;
import org.clintonhealthaccess.lmis.app.models.OrderType;

import java.text.SimpleDateFormat;
import java.util.Date;

@DatabaseTable
public class RoutineOrderAlert implements NotificationMessage {
    public static final String DATE_CREATED = "dateCreated";
    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = true, columnName = DATE_CREATED)
    private Date dateCreated;

    @DatabaseField(canBeNull = false)
    private boolean disabled;

    public RoutineOrderAlert() {
        //ormLite likes
    }

    public RoutineOrderAlert(Date date) {
        this.dateCreated = date;
        this.disabled = false;
    }

    @Override
    public String getMessage() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM");
        return "Place Routine Order for " + simpleDateFormat.format(dateCreated);
    }

    @Override
    public void onClick(Context context) {
        Log.i("Routine order alert clicked", getMessage());
        if (!disabled) {
            Intent intent = new Intent(context, OrderActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle data = new Bundle();
            data.putString(AlertClickListener.ORDER_TYPE, OrderType.ROUTINE);
            intent.putExtras(data);
            context.startActivity(intent);
        }
    }
}

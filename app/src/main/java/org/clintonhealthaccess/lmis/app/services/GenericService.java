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

package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.BaseItem;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static org.clintonhealthaccess.lmis.app.persistence.DbUtil.initialiseDao;

public class GenericService {

    public static <ActionClass, ItemClass extends BaseItem> int getTotal(Commodity commodity, Date startDate,
                                                                         Date endDate, Class<ActionClass> actionClass,
                                                                         Class<ItemClass> itemClass, Context context) {
        int totalQuantity = 0;

        List<ItemClass> items = getItems(commodity, startDate, endDate, actionClass, itemClass, context);

        for (ItemClass item : items) {
            totalQuantity += item.getQuantity();
        }

        return totalQuantity;
    }


    public static <ActionClass, ItemClass extends BaseItem> List<ItemClass> getItems(Commodity commodity, Date startDate,
                                                                         Date endDate, Class<ActionClass> actionClass,
                                                                         Class<ItemClass> itemClass, Context context) {
        SQLiteOpenHelper openHelper = getHelper(context, LmisSqliteOpenHelper.class);
        try {
            Dao<ActionClass, String> actionDao = initialiseDao(openHelper, actionClass);
            Dao<ItemClass, String> itemDao = initialiseDao(openHelper, itemClass);

            QueryBuilder<ActionClass, String> actionQueryBuilder = actionDao.queryBuilder();
            actionQueryBuilder.where().between("created", startDate, endDate);

            QueryBuilder<ItemClass, String> itemQueryBuilder = itemDao.queryBuilder();
            itemQueryBuilder.where().eq("commodity_id", commodity.getId());
            itemQueryBuilder.join(actionQueryBuilder);

            return itemQueryBuilder.query();

        } catch (SQLException e) {
            throw new LmisException(e);
        } finally {
            releaseHelper();
        }
    }

}


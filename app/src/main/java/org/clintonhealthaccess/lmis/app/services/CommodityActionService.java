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

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityAction;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.sql.SQLException;
import java.util.List;

public class CommodityActionService {

    @Inject
    Context context;

    @Inject
    DbUtil dbUtil;

    @Inject
    private LmisServer lmisServer;

    public CommodityActionService() {
    }

    public List<CommodityAction> getAllById(final List<String> ids) {
        return dbUtil.withDao(CommodityAction.class, new DbUtil.Operation<CommodityAction, List<CommodityAction>>() {
            @Override
            public List<CommodityAction> operate(Dao<CommodityAction, String> dao) throws SQLException {
                return dao.queryBuilder().where().in("id", ids).query();
            }
        });
    }

    protected void saveActionValues(final List<CommodityActionValue> commodityActionValues) {
        if (commodityActionValues != null) {
            dbUtil.withDaoAsBatch(CommodityActionValue.class, new DbUtil.Operation<CommodityActionValue, Void>() {
                        @Override
                        public Void operate(Dao<CommodityActionValue, String> dao) throws SQLException {
                            for (CommodityActionValue actionValue : commodityActionValues) {
                                dao.createOrUpdate(actionValue);
                            }
                            return null;
                        }
                    }

            );
        }
    }

    public void syncCommodityActionValues(User user, List<Commodity> commodities) {
        List<CommodityActionValue> commodityActionValues = lmisServer.fetchCommodityActionValues(commodities, user);
        saveActionValues(commodityActionValues);
    }
}

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

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import org.clintonhealthaccess.lmis.app.models.Allocation;
import org.clintonhealthaccess.lmis.app.models.AllocationItem;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;

import java.sql.SQLException;
import java.util.List;

import static com.google.common.collect.Lists.transform;

public class AllocationService {
    @Inject
    DbUtil dbUtil;

    @Inject
    Context context;

    public List<String> getYetToBeReceivedAllocationIds() {
        return dbUtil.withDao(Allocation.class, new DbUtil.Operation<Allocation, List<String>>() {
            @Override
            public List<String> operate(Dao<Allocation, String> dao) throws SQLException {
                return transform(queryAllocationsByReceived(dao, false), new Function<Allocation, String>() {
                    @Override
                    public String apply(Allocation input) {
                        return input.getAllocationId();
                    }
                });
            }
        });
    }

    private List<Allocation> queryAllocationsByReceived(Dao<Allocation, String> dao, boolean value) throws SQLException {
        QueryBuilder<Allocation, String> allocationStringQueryBuilder = dao.queryBuilder();
        allocationStringQueryBuilder.where().eq("received", value);
        PreparedQuery<Allocation> query = allocationStringQueryBuilder.prepare();
        return dao.query(query);
    }

    public List<String> getReceivedAllocationIds() {
        return dbUtil.withDao(Allocation.class, new DbUtil.Operation<Allocation, List<String>>() {
            @Override
            public List<String> operate(Dao<Allocation, String> dao) throws SQLException {
                return transform(queryAllocationsByReceived(dao, true), new Function<Allocation, String>() {
                    @Override
                    public String apply(Allocation input) {
                        return input.getAllocationId();
                    }
                });
            }
        });
    }

    public Allocation getAllocationByLmisId(final String allocationID) {
        return dbUtil.withDao(Allocation.class, new DbUtil.Operation<Allocation, Allocation>() {
            @Override
            public Allocation operate(Dao<Allocation, String> dao) throws SQLException {
                QueryBuilder<Allocation, String> allocationStringQueryBuilder = dao.queryBuilder();
                allocationStringQueryBuilder.where().eq("allocationId", allocationID);
                PreparedQuery<Allocation> query = allocationStringQueryBuilder.prepare();
                return dao.queryForFirst(query);
            }
        });
    }

    public void syncAllocations() {
        //FIXME: Just for test purposes
        GenericDao<Allocation> allocationGenericDao = new GenericDao<>(Allocation.class, context);
        GenericDao<AllocationItem> allocationItemGenericDao = new GenericDao<>(AllocationItem.class, context);
        GenericDao<Commodity> commodityDao = new GenericDao<>(Commodity.class, context);
        Allocation allocation = new Allocation();
        allocation.setReceived(false);
        allocation.setAllocationId("UG-2005");
        allocationGenericDao.create(allocation);

        AllocationItem item = new AllocationItem();
        item.setCommodity(commodityDao.queryForAll().get(0));
        item.setQuantity(10);
        item.setAllocation(allocation);
        allocationItemGenericDao.create(item);

    }

    public void update(Allocation allocation) {
        GenericDao<Allocation> allocationGenericDao = new GenericDao<>(Allocation.class, context);
        allocationGenericDao.update(allocation);
    }
}

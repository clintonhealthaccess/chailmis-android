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
import android.util.Log;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.Allocation;
import org.clintonhealthaccess.lmis.app.models.AllocationItem;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Multimaps.index;
import static java.lang.Integer.parseInt;
import static org.clintonhealthaccess.lmis.app.models.CommodityAction.ALLOCATION_ID;

public class AllocationService {
    @Inject
    private DbUtil dbUtil;

    @Inject
    private Context context;

    @Inject
    private LmisServer lmisServer;

    @Inject
    private CommodityService commodityService;

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

    public void update(Allocation allocation) {
        GenericDao<Allocation> allocationGenericDao = new GenericDao<>(Allocation.class, context);
        allocationGenericDao.update(allocation);
    }

    public void syncAllocations(User user) {
        List<Commodity> commodities = commodityService.all();
        List<CommodityActionValue> commodityActionValues = lmisServer.fetchAllocations(commodities, user);
        List<Allocation> allocations = toAllocations(commodityActionValues);
        for (Allocation allocation : allocations) {
            if (!isExisting(allocation)) {
                createAllocation(allocation);
            }
        }
    }

    private boolean isExisting(Allocation allocation) {
        Allocation existingAllocation = getAllocationByLmisId(allocation.getAllocationId());
        return existingAllocation != null;
    }

    private void createAllocation(Allocation allocation) {
        GenericDao<Allocation> allocationGenericDao = new GenericDao<>(Allocation.class, context);
        allocationGenericDao.create(allocation);

        GenericDao<AllocationItem> allocationItemGenericDao = new GenericDao<>(AllocationItem.class, context);
        for (AllocationItem allocationItem : allocation.getTransientAllocationItems()) {
            allocationItem.setAllocation(allocation);
            allocationItemGenericDao.create(allocationItem);
        }

        Log.i("Saved Allocation: ", allocation.getAllocationId() +
                " with " + allocation.getTransientAllocationItems().size() + " items");
    }

    private List<Allocation> toAllocations(List<CommodityActionValue> actionValues) {
        ImmutableList<Collection<CommodityActionValue>> groups = index(actionValues, new Function<CommodityActionValue, String>() {
            @Override
            public String apply(CommodityActionValue input) {
                return input.getPeriod();
            }
        }).asMap().values().asList();

        return transform(groups, new Function<Collection<CommodityActionValue>, Allocation>() {
            @Override
            public Allocation apply(Collection<CommodityActionValue> commodityActionValues) {
                return toAllocation(commodityActionValues);
            }
        });
    }

    private Allocation toAllocation(Collection<CommodityActionValue> commodityActionValues) {
        Collection<CommodityActionValue> filteredForAllocationId = filter(commodityActionValues, new Predicate<CommodityActionValue>() {
            @Override
            public boolean apply(CommodityActionValue commodityActionValue) {
                return ALLOCATION_ID.equals(commodityActionValue.getCommodityAction().getName());
            }
        });
        CommodityActionValue allocationIdValue = newArrayList(filteredForAllocationId).get(0);
        Allocation allocation = new Allocation(allocationIdValue.getValue(), allocationIdValue.getPeriod());

        ArrayList<CommodityActionValue> allocationActionValues = newArrayList(commodityActionValues);
        allocationActionValues.remove(allocationIdValue);
        List<AllocationItem> allocationItems = transform(allocationActionValues, new Function<CommodityActionValue, AllocationItem>() {
            @Override
            public AllocationItem apply(CommodityActionValue input) {
                AllocationItem allocationItem = new AllocationItem();
                try {
                    allocationItem.setQuantity(parseInt(input.getValue()));
                } catch (NumberFormatException e) {
                    throw new LmisException("Allocation Amount Not Integer: " + input.getCommodityAction().getActivityType() + " - " + input.getValue(), e);
                }
                allocationItem.setCommodity(input.getCommodityAction().getCommodity());
                return allocationItem;
            }
        });
        allocation.addTransientItems(allocationItems);

        return allocation;
    }

    public List<Allocation> getYetToBeReceivedAllocations() {
        return dbUtil.withDao(Allocation.class, new DbUtil.Operation<Allocation, List<Allocation>>() {
            @Override
            public List<Allocation> operate(Dao<Allocation, String> dao) throws SQLException {
                return queryAllocationsByReceived(dao, false);
            }
        });
    }
}

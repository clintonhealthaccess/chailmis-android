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
import com.thoughtworks.dhis.models.DataElementType;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Multimaps.index;
import static java.lang.Integer.parseInt;

public class AllocationService {
    @Inject
    private DbUtil dbUtil;

    @Inject
    private Context context;

    @Inject
    private LmisServer lmisServer;

    @Inject
    private CommodityService commodityService;

    private Dao<Allocation, String> dao;
    private static List<Allocation> allAllocations;

    public AllocationService(DbUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    public AllocationService() {
        //needed
    }


    public List<String> getYetToBeReceivedAllocationIds() {
        return from(queryAllocationsByReceived(false))
                .transform(new Function<Allocation, String>() {
                    @Override
                    public String apply(Allocation input) {
                        return input.getAllocationId();
                    }
                }).toList();
    }

    private List<Allocation> queryAllocationsByReceived(final boolean value) {
        return from(all()).filter(
                new Predicate<Allocation>() {
                    @Override
                    public boolean apply(Allocation input) {
                        return input.isReceived() == value;
                    }
                }
        ).toList();
    }

    public List<String> getReceivedAllocationIds() {
        return from(queryAllocationsByReceived(true)).transform(
                new Function<Allocation, String>() {
                    @Override
                    public String apply(Allocation input) {
                        return input.getAllocationId();
                    }
                }
        ).toList();
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
        List<CommodityActionValue> commodityActionValues = lmisServer.fetchAllocations(user);
        List<Allocation> allocations = toAllocations(commodityActionValues);

        String facilityName = user.getFacilityName() == null ? "" : user.getFacilityName();
        String facility2LetterCode = facilityName.length() > 2 ?
                facilityName.substring(0, 2).toUpperCase() :
                facilityName.toUpperCase();

        boolean changed = false;
        if (allocations != null) {
            for (Allocation allocation : allocations) {
                // skip invalid allocation
                if(!validateAllocationId(facility2LetterCode ,allocation.getAllocationId())){
                    Log.e("syncAllocations", "Allocation Id (" + allocation.getAllocationId() + ") is not valid , skiped");
                    continue;
                }

                if (!isExisting(allocation)) {
                    createAllocation(allocation);
                    changed = true;
                }
            }
        }
        if (changed) {
            resetCache();
        }
    }

    private boolean validateAllocationId(String facility2LetterCode, String allocationId){
        String patternString = facility2LetterCode.toUpperCase() + "\\d+$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(allocationId);
        return matcher.matches();
    }

    private void resetCache() {
        clearCache();
        all();
    }

    private boolean isExisting(Allocation allocation) {
        Allocation existingAllocation = getAllocationByLmisId(allocation.getAllocationId());
        return existingAllocation != null;
    }

    public void createAllocation(Allocation allocation) {
        Allocation existingAllocation = getAllocationByLmisId(allocation.getAllocationId());
        if (existingAllocation != null) {
            existingAllocation.setPeriod(allocation.getPeriod());
            existingAllocation.setTransientAllocationItems(allocation.getTransientAllocationItems());
        } else {
            existingAllocation = allocation;
        }

        GenericDao<Allocation> allocationGenericDao = new GenericDao<>(Allocation.class, context);
        allocationGenericDao.createOrUpdate(existingAllocation);

        GenericDao<AllocationItem> allocationItemGenericDao = new GenericDao<>(AllocationItem.class, context);
        for (AllocationItem allocationItem : existingAllocation.getTransientAllocationItems()) {
            allocationItem.setAllocation(allocation);
            allocationItemGenericDao.create(allocationItem);
        }

        Log.i("Saved Allocation: ", allocation.getAllocationId() +
                " with " + allocation.getTransientAllocationItems().size() + " items");
        //TODO find and update CommodityActionValues - For - This Allocation
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
                return DataElementType.ALLOCATION_ID.getActivity().equals(commodityActionValue.getCommodityAction().getName());
            }
        });

        if(filteredForAllocationId == null || filteredForAllocationId.size() == 0){
            return null;
        }

        CommodityActionValue allocationIdValue = newArrayList(filteredForAllocationId).get(0);
        Allocation allocation = new Allocation(allocationIdValue.getValue(), allocationIdValue.getPeriod());

        ArrayList<CommodityActionValue> allocationActionValues = newArrayList(commodityActionValues);
        allocationActionValues.remove(allocationIdValue);

        List<AllocationItem> allocationItems = new ArrayList<>();
        for(CommodityActionValue input : allocationActionValues){
            // ignore incorrect allocation data.
            try{
                AllocationItem allocationItem = new AllocationItem();
                allocationItem.setQuantity(parseInt(input.getValue()));
                // skip no commodity item.
                if (input.getCommodityAction().getCommodity() == null){
                    Log.e("ToAllocation", "Skip AllocationItem because commodity is null => id: " + input.getCommodityAction().getId());
                    continue;
                }
                allocationItem.setCommodity(input.getCommodityAction().getCommodity());
                allocationItems.add(allocationItem);
            }catch (NumberFormatException e){
                Log.e("ToAllocation", "AllocationItem(" + input.getCommodityAction().getId() +  ") Quantity Not Integer: " + input.getCommodityAction().getActivityType() + " - " + input.getValue());
            }
        }

        allocation.addTransientItems(allocationItems);

        return allocation;
    }

    public List<Allocation> getYetToBeReceivedAllocations() {
        return queryAllocationsByReceived(false);
    }

    public List<Allocation> all() {
        if (allAllocations == null) {
            allAllocations = dbUtil.withDao(Allocation.class, new DbUtil.Operation<Allocation, List<Allocation>>() {
                @Override
                public List<Allocation> operate(Dao<Allocation, String> dao) throws SQLException {
                    return dao.queryForAll();
                }
            });
        }
        return allAllocations;
    }

    public static void clearCache() {
        allAllocations = null;
    }
}

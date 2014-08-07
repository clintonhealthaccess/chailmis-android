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
        GenericDao<Allocation> allocationGenericDao = new GenericDao<>(Allocation.class, context);
        GenericDao<AllocationItem> allocationItemGenericDao = new GenericDao<>(AllocationItem.class, context);
        GenericDao<Commodity> commodityDao = new GenericDao<>(Commodity.class, context);
        Allocation allocation = new Allocation();
        allocation.setReceived(false);
        allocation.setAllocationId("UG-2005");
        allocationGenericDao.create(allocation);

        AllocationItem item = new AllocationItem();
        item.setCommodity(commodityDao.getById("1"));
        item.setQuantity(10);
        item.setAllocation(allocation);
        allocationItemGenericDao.create(item);

    }
}

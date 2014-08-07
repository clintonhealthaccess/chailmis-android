package org.clintonhealthaccess.lmis.app.services;

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import org.clintonhealthaccess.lmis.app.models.Allocation;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;

import java.sql.SQLException;
import java.util.List;

import static com.google.common.collect.Lists.transform;

public class AllocationService {
    @Inject
    DbUtil dbUtil;

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
}

package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;

import java.sql.SQLException;
import java.util.List;

public class CategoryService {

    @Inject
    private DbUtil dbUtil;

    public List<Category> all() {
        return dbUtil.withDao(Category.class, new DbUtil.Operation<Category, List<Category>>() {
            @Override
            public List<Category> operate(Dao<Category, String> dao) throws SQLException {
                return dao.queryForAll();
            }
        });
    }
}

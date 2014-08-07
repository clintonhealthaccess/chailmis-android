package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.persistence.DbUtil;

import java.sql.SQLException;
import java.util.List;

import roboguice.RoboGuice;

public class GenericDao<Model> {
    @Inject
    DbUtil dbUtil;
    private Class<Model> type;

    public GenericDao(Class<Model> type, Context context) {
        this.type = type;
        RoboGuice.getInjector(context).injectMembers(this);
    }

    public Model create(final Model object) {
        return dbUtil.withDao(type, new DbUtil.Operation<Model, Model>() {
            @Override
            public Model operate(Dao<Model, String> dao) throws SQLException {
                dao.create(object);
                return object;
            }
        });
    }

    public List<Model> queryForAll() {
        return dbUtil.withDao(type, new DbUtil.Operation<Model, List<Model>>() {
            @Override
            public List<Model> operate(Dao<Model, String> dao) throws SQLException {
                return dao.queryForAll();
            }
        });
    }

    public Integer update(final Model object) {
        return dbUtil.withDao(type, new DbUtil.Operation<Model, Integer>() {
            @Override
            public Integer operate(Dao<Model, String> dao) throws SQLException {
                return dao.update(object);
            }
        });
    }

    public Model getById(final String id) {
        return dbUtil.withDao(type, new DbUtil.Operation<Model, Model>() {
            @Override
            public Model operate(Dao<Model, String> dao) throws SQLException {
                return dao.queryForId(id);
            }
        });
    }
}

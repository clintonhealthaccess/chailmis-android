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

    public Model createOrUpdate(final Model object) {
        return dbUtil.withDao(type, new DbUtil.Operation<Model, Model>() {
            @Override
            public Model operate(Dao<Model, String> dao) throws SQLException {
                dao.createOrUpdate(object);
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

    public long countOf() {
        return dbUtil.withDao(type, new DbUtil.Operation<Model, Long>() {
            @Override
            public Long operate(Dao<Model, String> dao) throws SQLException {
                return dao.countOf();
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

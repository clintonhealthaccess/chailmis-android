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

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.UserProfile;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.app.sync.SyncManager;

import java.sql.SQLException;
import java.util.List;

import static org.clintonhealthaccess.lmis.app.persistence.DbUtil.Operation;

public class UserService {
    @Inject
    private DbUtil dbUtil;

    @Inject
    private LmisServer lmisServer;

    @Inject
    private SyncManager syncManager;

    @Inject
    private Context context;

    public boolean userRegistered() {
        return dbUtil.withDao(User.class, new Operation<User, Boolean>() {
            @Override
            public Boolean operate(Dao<User, String> dao) throws SQLException {
                return dao.countOf() > 0;
            }
        });
    }

    public User register(final String username, final String password) {
        UserProfile profile = lmisServer.validateLogin(new User(username, password));

        User user = new User(username, password);
        if (profile.getOrganisationUnits().size() > 0) {
            user.setFacilityCode(profile.getOrganisationUnits().get(0).getId());
            user.setFacilityName(profile.getOrganisationUnits().get(0).getName());
            //updateUser(user);
        } else {
            user.setFacilityCode("BLANK");
            user.setFacilityName("BLANK");
            Log.e("Error", "No organisations found for " + username);
        }

        saveUserToDatabase(user);

        syncManager.createSyncAccount(user);
        return user;
    }

    public User saveUserToDatabase(final User user) {
        return dbUtil.withDao(context, User.class, new Operation<User, User>() {
            @Override
            public User operate(Dao<User, String> dao) throws SQLException {
                dao.create(user);
                return user;
            }
        });
    }

    public User getRegisteredUser() throws IndexOutOfBoundsException {
        return dbUtil.withDao(context, User.class, new Operation<User, User>() {
            @Override
            public User operate(Dao<User, String> dao) throws SQLException {
                List<User> users = dao.queryForAll();
                return users.get(0);
            }
        });
    }
}

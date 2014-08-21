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

package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;
import static com.j256.ormlite.table.TableUtils.clearTable;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class UserTest {
    private Dao<User, Long> userDao;
    private AndroidConnectionSource connectionSource;
    private LmisSqliteOpenHelper openHelper;

    @Before
    public void setUp() throws SQLException {
        setUpInjection(this);

        openHelper = getHelper(application, LmisSqliteOpenHelper.class);
        connectionSource = new AndroidConnectionSource(openHelper);
        userDao = createDao(connectionSource, User.class);
    }

    @After
    public void tearDown() throws Exception {
        clearTable(connectionSource, User.class);
        releaseHelper();
    }

    @Test
    public void testShouldBeAbleToCRUD() throws Exception {
        assertThat(userDao.countOf(), is(0l));

        User user = new User("admin", "district");
        int newUserId = userDao.create(user);
        assertThat(newUserId, is(1));
        assertThat(userDao.countOf(), is(1l));
    }

    @Test
    public void shouldEncodeUsernameAndPasswordForBasicAuth() throws Exception {
        User user = new User("username", "password");
        assertThat(user.encodeCredentialsForBasicAuthorization(), is("Basic dXNlcm5hbWU6cGFzc3dvcmQ="));
    }
}
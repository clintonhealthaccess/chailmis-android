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

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.sync.SyncManager;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.sql.SQLException;

import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.Robolectric.addPendingHttpResponse;

@RunWith(RobolectricGradleTestRunner.class)
public class UserServiceTest {
    @Inject
    private UserService userService;

    @Before
    public void setUp() throws SQLException {
        final SyncManager mockSyncManager = mock(SyncManager.class);
        AbstractModule moduleForMockSyncManager = new AbstractModule() {
            @Override
            protected void configure() {
                bind(SyncManager.class).toInstance(mockSyncManager);
            }
        };
        setUpInjection(this, moduleForMockSyncManager);
    }

    @After
    public void tearDown() {
        releaseHelper();
    }

    @Test
    public void testShouldKnowIfThereIsUserRegistered() throws Exception {
        assertThat(userService.userRegistered(), is(false));

        addPendingHttpResponse(200, Robolectric.application.getString(R.string.user_profile_demo_response));

        User newUser = userService.register("admin", "district");

        assertThat(newUser, not(nullValue()));
        assertThat(userService.userRegistered(), is(true));
    }

    @Test
    public void shouldSaveTheOrgUnitIfAvailable() throws Exception {

        addPendingHttpResponse(200, Robolectric.application.getString(R.string.user_profile_demo_response));

        User newUser = userService.register("admin", "district");

        assertThat(newUser.getFacilityName(), is("cr Abi LGA Staff Clinic"));
        assertThat(newUser.getFacilityCode(), is("U5Zz9lIqxpt"));
    }


    @Test(expected = LmisException.class)
    public void testShouldDisallowRegisteringWithInvalidDHISCredentials() throws Exception {
        assertThat(userService.userRegistered(), is(false));

        addPendingHttpResponse(403, "Rejected");

        userService.register("admin", "district");
    }
}
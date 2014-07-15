package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.apache.http.client.methods.HttpUriRequest;
import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.sync.SyncManager;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.sql.SQLException;

import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.Robolectric.addPendingHttpResponse;
import static org.robolectric.Robolectric.getSentHttpRequest;

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

        addPendingHttpResponse(200, "OK");

        User newUser = userService.register("admin", "district");

        assertThat(newUser, not(nullValue()));
        assertThat(userService.userRegistered(), is(true));

        URI requestedUri = ((HttpUriRequest) getSentHttpRequest(0)).getURI();
        URI expectedUri = URI.create("http://104.131.225.22:8888/dhis2/api/users");
        assertThat(requestedUri, equalTo(expectedUri));
    }

    @Test(expected = LmisException.class)
    public void testShouldDisallowRegisteringWithInvalidDHISCredentials() throws Exception {
        assertThat(userService.userRegistered(), is(false));

        addPendingHttpResponse(403, "Rejected");

        userService.register("admin", "district");
    }
}
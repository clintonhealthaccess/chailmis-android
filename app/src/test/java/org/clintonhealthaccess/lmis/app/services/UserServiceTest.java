package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class UserServiceTest {
    @Inject
    private UserService userService;

    @Before
    public void setUp() throws SQLException {
        setUpInjection(this);
    }

    @Test
    public void testShouldKnowIfThereIsUserRegistered() throws Exception {
        assertThat(userService.userRegistered(), is(false));

        User newUser = userService.register("admin", "district");
        assertThat(newUser, not(nullValue()));

        assertThat(userService.userRegistered(), is(true));
    }
}
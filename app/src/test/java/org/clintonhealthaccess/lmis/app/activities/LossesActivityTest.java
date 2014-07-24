package org.clintonhealthaccess.lmis.app.activities;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class LossesActivityTest {


    private UserService userService;

    private LossesActivity getLossesActivity() {
        return buildActivity(LossesActivity.class).create().get();
    }

    @Before
    public void setUp() throws Exception {
        userService = mock(UserService.class);
        when(userService.getRegisteredUser()).thenReturn(new User("", "", "place"));
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
            }
        });

    }

    @Test
    public void testBuildActivity() throws Exception {
        LossesActivity lossesActivity = getLossesActivity();
        assertThat(lossesActivity, not(nullValue()));
    }
}

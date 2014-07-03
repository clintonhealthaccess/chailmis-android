package org.clintonhealthaccess.lmis.app.activities;

import android.content.Intent;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.RobolectricGradleTestRunner;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.shadowOf;
import static roboguice.RoboGuice.DEFAULT_STAGE;
import static roboguice.RoboGuice.newDefaultRoboModule;
import static roboguice.RoboGuice.setBaseApplicationInjector;

@RunWith(RobolectricGradleTestRunner.class)
public class HomeActivityTest {
    private HomeActivity homeActivity;

    @Before
    public void setUp() {
    }

    @Test
    public void testBuildActivity() throws Exception {
        homeActivity = buildActivity(HomeActivity.class).create().get();
        assertThat(homeActivity, not(nullValue()));
    }

    @Test
    public void testShouldRenderRegisterActivityIfThereIsNoUserRegistered() throws Exception {
        UserService mockUserService = mock(UserService.class);
        when(mockUserService.userRegistered()).thenReturn(false);

        setBaseApplicationInjector(application, DEFAULT_STAGE, newDefaultRoboModule(application), new MockedModule(mockUserService));
        RoboInjector injector = RoboGuice.getInjector(application);
        injector.injectMembersWithoutViews(this);

        homeActivity = buildActivity(HomeActivity.class).create().get();

        Intent expectedIntent = new Intent(homeActivity, RegisterActivity.class);
        assertThat(shadowOf(homeActivity).getNextStartedActivity(), equalTo(expectedIntent));
    }

    public class MockedModule extends AbstractModule {
        private UserService userService;

        public MockedModule(UserService userService) {
            this.userService = userService;
        }

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
        }
    }
}

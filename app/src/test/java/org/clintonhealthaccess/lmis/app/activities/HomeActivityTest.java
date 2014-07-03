package org.clintonhealthaccess.lmis.app.activities;

import android.content.Intent;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
public class HomeActivityTest {
    @Test
    public void testBuildActivity() throws Exception {
        HomeActivity homeActivity = buildActivity(HomeActivity.class).create().get();
        assertThat(homeActivity, not(nullValue()));
    }

    @Test
    public void testShouldRenderRegisterActivityIfThereIsNoUserRegistered() throws Exception {
        final UserService mockUserService = mock(UserService.class);
        when(mockUserService.userRegistered()).thenReturn(false);

        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(mockUserService);
            }
        });

        HomeActivity homeActivity = buildActivity(HomeActivity.class).create().get();

        Intent registerIntent = new Intent(homeActivity, RegisterActivity.class);
        assertThat(shadowOf(homeActivity).getNextStartedActivity(), equalTo(registerIntent));
    }
}

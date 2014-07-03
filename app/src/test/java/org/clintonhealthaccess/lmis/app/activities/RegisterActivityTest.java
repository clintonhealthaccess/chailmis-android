package org.clintonhealthaccess.lmis.app.activities;

import android.content.Intent;
import android.widget.Button;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
public class RegisterActivityTest {
    private RegisterActivity registerActivity;
    private UserService mockUserService;

    @Before
    public void setUp() throws Exception {
        mockUserService = mock(UserService.class);

        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(mockUserService);
            }
        });

        registerActivity = buildActivity(RegisterActivity.class).create().get();
    }

    @Test
    public void testShouldRedirectToHomePageAfterSuccessfulRegistration() throws Exception {
        when(mockUserService.register(anyString(), anyString())).thenReturn(new User());

        Button registerButton = (Button) registerActivity.findViewById(R.id.buttonRegister);
        registerButton.performClick();

        Intent homeIntent = new Intent(registerActivity, HomeActivity.class);
        assertThat(shadowOf(registerActivity).getNextStartedActivity(), equalTo(homeIntent));
    }
}
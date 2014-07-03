package org.clintonhealthaccess.lmis.app.activities;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.ServiceException;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

        fillTextField(R.id.textUsername, "admin");
        fillTextField(R.id.textPassword, "district");
        getRegisterButton().performClick();

        Intent homeIntent = new Intent(registerActivity, HomeActivity.class);
        assertThat(shadowOf(registerActivity).getNextStartedActivity(), equalTo(homeIntent));
    }

    @Test
    public void testShouldNotRegisterIfUsernameOrPasswordIsAbsent() throws Exception {
        getRegisterButton().performClick();
        verify(mockUserService, never()).register(anyString(), anyString());

        fillTextField(R.id.textUsername, "admin");
        getRegisterButton().performClick();
        verify(mockUserService, never()).register(anyString(), anyString());

        fillTextField(R.id.textUsername, "");
        fillTextField(R.id.textPassword, "district");
        getRegisterButton().performClick();
        verify(mockUserService, never()).register(anyString(), anyString());
    }

    @Test
    public void testShouldStayOnSamePageIfRegistrationFails() throws Exception {
        when(mockUserService.register(anyString(), anyString())).thenThrow(new ServiceException());

        fillTextField(R.id.textUsername, "admin");
        fillTextField(R.id.textPassword, "district");
        getRegisterButton().performClick();

        assertThat(shadowOf(registerActivity).getNextStartedActivity(), nullValue());
    }

    private void fillTextField(int inputFieldId, String text) {
        TextView usernameInputField = (TextView) registerActivity.findViewById(inputFieldId);
        usernameInputField.setText(text);
    }

    private Button getRegisterButton() {
        return (Button) registerActivity.findViewById(R.id.buttonRegister);
    }
}
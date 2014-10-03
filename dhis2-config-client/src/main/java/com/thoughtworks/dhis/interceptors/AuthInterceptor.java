package com.thoughtworks.dhis.interceptors;

import com.squareup.okhttp.Credentials;
import com.thoughtworks.dhis.models.User;
import retrofit.RequestInterceptor;

public class AuthInterceptor implements RequestInterceptor {
    private User user;

    public AuthInterceptor(User user) {
        this.user = user;
    }

    @Override
    public void intercept(RequestFacade requestFacade) {

        if (user != null) {
            final String authorizationValue = encodeCredentialsForBasicAuthorization();
            requestFacade.addHeader("Authorization", authorizationValue);
        }
    }

    private String encodeCredentialsForBasicAuthorization() {
        return Credentials.basic(user.getUsername(), user.getPassword());

    }

}

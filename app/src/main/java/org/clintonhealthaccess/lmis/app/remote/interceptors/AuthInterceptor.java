package org.clintonhealthaccess.lmis.app.remote.interceptors;

import org.clintonhealthaccess.lmis.app.models.User;

import retrofit.RequestInterceptor;

public class AuthInterceptor implements RequestInterceptor {

    private User user;

    public AuthInterceptor(User user) {
        this.user = user;
    }

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Authorization", user.encodeCredentialsForBasicAuthorization());
    }

}

package com.thoughtworks.dhis;

import com.thoughtworks.dhis.endpoints.ApiService;
import com.thoughtworks.dhis.interceptors.AuthInterceptor;
import com.thoughtworks.dhis.models.User;
import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {

    private String endpoint;
    private User user;

    public Client(String endpoint, User user) {
        this.endpoint = endpoint;
        this.user = user;
    }

    public ApiService getService() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(new AuthInterceptor(user))
                .setErrorHandler(new ErrorHandler() {
                    @Override
                    public Throwable handleError(RetrofitError retrofitError) {
                        try {

                        } catch (NullPointerException ex) {

                        }
                        return new Exception(retrofitError.getMessage());
                    }
                })
                .build();
        return restAdapter.create(ApiService.class);
    }

    private String getBody(RetrofitError retrofitError) {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {

            reader = new BufferedReader(new InputStreamReader(retrofitError.getResponse().getBody().in()));

            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}

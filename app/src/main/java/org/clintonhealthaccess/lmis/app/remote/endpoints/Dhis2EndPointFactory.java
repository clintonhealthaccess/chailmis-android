package org.clintonhealthaccess.lmis.app.remote.endpoints;

import android.content.Context;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.remote.interceptors.AuthInterceptor;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.ApacheClient;
import retrofit.client.Header;
import roboguice.inject.InjectResource;

import static android.util.Log.e;
import static android.util.Log.i;
import static org.apache.http.HttpStatus.SC_OK;

public class Dhis2EndPointFactory {
    @InjectResource(R.string.message_invalid_login_credential)
    private String messageInvalidLoginCredential;

    @InjectResource(R.string.message_network_error)
    private String messageNetworkError;

    @InjectResource(R.string.dhis2_base_url)
    private String dhis2BaseUrl;

    @Inject
    private Context context;

    public Dhis2Endpoint create(User user) {
        RestAdapter restAdapter = makeRestAdapter(user);
        return restAdapter.create(Dhis2Endpoint.class);
    }

    private RestAdapter makeRestAdapter(User user) {
        AuthInterceptor requestInterceptor = new AuthInterceptor(user);
        return new RestAdapter.Builder()
                .setRequestInterceptor(requestInterceptor)
                .setErrorHandler(new Dhis2ErrorHandler())
                .setEndpoint(dhis2BaseUrl)
                .setClient(new ApacheClient())
                .build();
    }

    private class Dhis2ErrorHandler implements ErrorHandler {
        @Override
        public Throwable handleError(RetrofitError cause) {
            if(cause.getResponse() == null) {
                return new LmisException(cause);
            }

            e("Error DHIS2 reason", cause.getResponse().getReason());
            e("Error DHIS2 url", cause.getResponse().getUrl());
            for (Header header : cause.getResponse().getHeaders()) {
                e("Error DHIS2 header", String.format("%s : %s", header.getName(), header.getValue()));
            }
            if (cause.isNetworkError()) {
                i("Failed to connect DHIS2 server.", cause.getMessage());
                return new LmisException(messageNetworkError);
            }

            int statusCode = cause.getResponse().getStatus();
            if (statusCode != SC_OK) {
                i("Failed attempt to login.", "Response code : " + statusCode);
                return new LmisException(messageInvalidLoginCredential);
            }
            return new LmisException(cause.getMessage());
        }
    }

}

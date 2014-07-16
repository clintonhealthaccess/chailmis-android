package org.clintonhealthaccess.lmis.app.remote;

import android.content.Context;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.remote.endpoints.Dhis2Endpoint;
import org.clintonhealthaccess.lmis.app.remote.interceptors.AuthInterceptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.ApacheClient;
import retrofit.client.Header;
import roboguice.inject.InjectResource;

import static android.util.Log.e;
import static android.util.Log.i;
import static java.util.Arrays.asList;
import static org.apache.http.HttpStatus.SC_OK;

public class Dhis2 implements LmisServer {
    @InjectResource(R.string.dhis2_base_url)
    private String dhis2BaseUrl;

    @InjectResource(R.string.message_invalid_login_credential)
    private String messageInvalidLoginCredential;

    @InjectResource(R.string.message_network_error)
    private String messageNetworkError;

    @Inject
    private Context context;

    @Override
    public void validateLogin(User user) {
        RestAdapter restAdapter = makeRestAdapter(user);
        Dhis2Endpoint service = restAdapter.create(Dhis2Endpoint.class);
        service.getUsers();
    }

    @Override
    public List<Category> fetchCommodities() {
        // FIXME: should fetch from DHIS2 and populate JSON
        String defaultCommoditiesAsJson;
        try {
            InputStream src = context.getAssets().open("default_commodities.json");
            defaultCommoditiesAsJson = CharStreams.toString(new InputStreamReader(src));
        } catch (IOException e) {
            throw new LmisException("Doesn't matter, we will change this anyway.", e);
        }
        return asList(new Gson().fromJson(defaultCommoditiesAsJson, Category[].class));
    }

    @Override
    public Map<String, List<String>> fetchOrderReasons(User user) {
        RestAdapter restAdapter = makeRestAdapter(user);
        Dhis2Endpoint service = restAdapter.create(Dhis2Endpoint.class);
        return service.getReasonsForOrder();

    }

    private RestAdapter makeRestAdapter(User user) {
        AuthInterceptor requestInterceptor = new AuthInterceptor(user);
        return new RestAdapter.Builder()
                .setRequestInterceptor(requestInterceptor)
                .setErrorHandler(new Dhis2ErrorHandler())
                .setEndpoint(context.getString(R.string.dhis2_base_url))
                .setClient(new ApacheClient())
                .build();
    }


    private class Dhis2ErrorHandler implements ErrorHandler {
        @Override
        public Throwable handleError(RetrofitError cause) {
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

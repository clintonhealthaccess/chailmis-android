package org.clintonhealthaccess.lmis.app.remote;

import android.content.Context;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.inject.Inject;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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

import retrofit.RestAdapter;
import retrofit.client.ApacheClient;
import roboguice.inject.InjectResource;

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
        HttpGet request = new HttpGet(dhis2BaseUrl + "/api/users");
        request.addHeader("Authorization", user.encodeCredentialsForBasicAuthorization());

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            i("Failed to connect DHIS2 server.", e.getMessage());
            throw new LmisException(messageNetworkError);
        }

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != SC_OK) {
            i("Failed attempt to login.", "Response code : " + statusCode);
            throw new LmisException(messageInvalidLoginCredential);
        }
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
    public List<String> fetchOrderReasons(User user) {
        RestAdapter restAdapter = makeRestAdapter(user);
        Dhis2Endpoint service = restAdapter.create(Dhis2Endpoint.class);
        return service.getReasonsForOrder();

    }

    private RestAdapter makeRestAdapter(User user) {
        AuthInterceptor requestInterceptor = new AuthInterceptor(user);
        return new RestAdapter.Builder()
                .setRequestInterceptor(requestInterceptor)
                .setEndpoint(context.getString(R.string.dhis2_base_url))
                .setClient(new ApacheClient())
                .build();
    }


}

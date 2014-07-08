package org.clintonhealthaccess.lmis.app.remote;

import android.content.Context;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import roboguice.inject.InjectResource;

import static android.util.Base64.NO_WRAP;
import static android.util.Base64.encodeToString;
import static android.util.Log.i;
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
    public void validateLogin(String username, String password) {
        String credentials = username + ":" + password;
        String base64EncodedCredentials = encodeToString(credentials.getBytes(), NO_WRAP);
        HttpGet request = new HttpGet(dhis2BaseUrl + "/api/users");
        request.addHeader("Authorization", "Basic " + base64EncodedCredentials);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            i("Failed to connect DHIS2 server.", e.getMessage());
            throw new LmisException(messageNetworkError);
        }

        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode != SC_OK) {
            i("Failed attempt to login.", "Response code : " + statusCode);
            throw new LmisException(messageInvalidLoginCredential);
        }
    }

    @Override
    public String fetchCommodities() {
        // FIXME: should fetch from DHIS2 and populate JSON
        try {
            InputStream src = context.getAssets().open("default_commodities.json");
            return CharStreams.toString(new InputStreamReader(src));
        } catch (IOException e) {
            throw new LmisException("Doesn't matter, we will change this anyway.", e);
        }
    }
}

package org.clintonhealthaccess.lmis.app.remote;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.services.ServiceException;

import java.io.IOException;

import roboguice.inject.InjectResource;

import static android.util.Base64.NO_WRAP;
import static android.util.Base64.encodeToString;
import static android.util.Log.i;
import static org.apache.http.HttpStatus.SC_OK;

public class Dhis2 implements LmisServer {
    @InjectResource(R.string.dhis2_base_url)
    private String dhis2BaseUrl;

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
            throw new ServiceException("Network issue. Please try again later.");
        }

        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode != SC_OK) {
            i("Failed attempt to login.", "Response code : " + statusCode);
            throw new ServiceException("Please input correct username / password combination");
        }
    }
}

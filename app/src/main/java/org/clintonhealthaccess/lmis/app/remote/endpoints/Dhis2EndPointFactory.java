/*
 * Copyright (c) 2014, Thoughtworks Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

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
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;

public class Dhis2EndPointFactory {
    @InjectResource(R.string.message_invalid_login_credential)
    private String messageInvalidLoginCredential;

    @InjectResource(R.string.url_not_found)
    private String urlNotFound;

    @InjectResource(R.string.internal_server_error)
    private String internalServerError;

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
            if (cause.getResponse() == null) {
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

            if (statusCode == SC_NOT_FOUND) {
                i("Failed attempt to login.", "Response code : " + statusCode);
                return new LmisException(urlNotFound);
            }
            if (statusCode == SC_INTERNAL_SERVER_ERROR) {
                i("Internal Server Error", "Response code : " + statusCode);
                return new LmisException(internalServerError);
            }
            if (statusCode != SC_OK) {
                i("Failed attempt to login.", "Response code : " + statusCode);
                return new LmisException(messageInvalidLoginCredential);
            }

            return new LmisException(cause.getMessage());
        }
    }

}

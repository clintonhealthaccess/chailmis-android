package org.clintonhealthaccess.lmis.app.remote.endpoints;

import org.clintonhealthaccess.lmis.app.BuildConfig;
import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.R;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.ApacheClient;
import retrofit.converter.SimpleXMLConverter;
import roboguice.inject.InjectResource;

public class FDroidEndPointFactory {

    @InjectResource(R.string.app_market_base_url)
    private String appMarketHost;

    @InjectResource(R.string.message_network_error)
    private String messageNetworkError;

    public FDroidEndPoint getEndPoint() {
        return new RestAdapter.Builder()
                .setConverter(new SimpleXMLConverter())
                .setErrorHandler(new FDroidErrorHandler())
                .setEndpoint(appMarketHost)
                .setClient(new ApacheClient())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build().create(FDroidEndPoint.class);
    }

    private class FDroidErrorHandler implements ErrorHandler {
        @Override
        public Throwable handleError(RetrofitError cause) {
            if (cause.isNetworkError()) {
                return new LmisException(messageNetworkError);
            } else {
                return new LmisException(cause.getMessage());
            }
        }
    }
}

package org.clintonhealthaccess.lmis.app.remote;

import android.content.Context;

import com.google.common.base.Function;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.CategoryCombo;
import org.clintonhealthaccess.lmis.app.models.CategoryOptionCombos;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.DataElement;
import org.clintonhealthaccess.lmis.app.models.DataSet;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.remote.endpoints.Dhis2Endpoint;
import org.clintonhealthaccess.lmis.app.remote.interceptors.AuthInterceptor;

import java.util.ArrayList;
import java.util.Collection;
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
import static com.google.common.collect.Collections2.transform;
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
        service.validateLogin();
    }

    @Override
    public List<Category> fetchCommodities() {

        RestAdapter restAdapter = makeRestAdapter(new User("tw_test", "Secret123"));
        Dhis2Endpoint service = restAdapter.create(Dhis2Endpoint.class);
        DataSet dataSet = service.getDataSet("wXidpxeF08C");
        List<DataElement> elements = new ArrayList<>();
        for (DataElement element : dataSet.getDataElements()) {
            i("Element", element.getName());
            element = service.getDataElement(element.getId());
            element.setCategoryCombo(service.getCategoryCombo(element.getCategoryCombo().getId()));
            elements.add(element);
        }
        Collection<Category> cats =
                transform(elements, new Function<DataElement, Category>() {
                    @Override
                    public Category apply(DataElement element) {
                        Category cat = new Category(element.getName());
                        for (CategoryOptionCombos option : element.getCategoryCombo().getCategoryOptionCombos()) {
                            cat.addCommodity(new Commodity(option.getId(), option.getName()));
                        }
                        return cat;
                    }
                });
        return new ArrayList<>(cats);
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

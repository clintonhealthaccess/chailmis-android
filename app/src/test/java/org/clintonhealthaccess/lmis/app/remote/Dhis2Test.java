package org.clintonhealthaccess.lmis.app.remote;

import com.google.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.tester.org.apache.http.TestHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import roboguice.inject.InjectResource;

import static org.clintonhealthaccess.lmis.app.models.OrderReason.ORDER_REASONS_JSON_KEY;
import static org.clintonhealthaccess.lmis.app.models.OrderReason.UNEXPECTED_QUANTITY_JSON_KEY;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.robolectric.Robolectric.addHttpResponseRule;
import static org.robolectric.Robolectric.addPendingHttpResponse;
import static org.robolectric.Robolectric.getSentHttpRequest;

@RunWith(RobolectricGradleTestRunner.class)
public class Dhis2Test {
    @InjectResource(R.string.dhis2_base_url)
    private String dhis2BaseUrl;

    @Inject
    private Dhis2 dhis2;

    @Before
    public void setUp() throws Exception {
        setUpInjection(this);
    }

    @Test
    public void testShouldValidateUserLogin() throws Exception {
        addPendingHttpResponse(200, "OK");

        User user = new User("test", "pass");
        dhis2.validateLogin(user);

        HttpRequest lastSentHttpRequest = getSentHttpRequest(0);
        assertThat(lastSentHttpRequest.getRequestLine().getUri(), equalTo(dhis2BaseUrl + "/api/dataSets"));
        Header authorizationHeader = lastSentHttpRequest.getFirstHeader("Authorization");
        assertThat(authorizationHeader.getValue(), equalTo("Basic dGVzdDpwYXNz"));
    }

    @Test
    public void testShouldFetchCommoditiesFromDhis2() throws Exception {
        setUpSuccessHttpGetRequest("/api/dataSets/wXidpxeF08C", "dataSetForCommodities.json");
        setUpSuccessHttpGetRequest("/api/dataElements/lojV8nnoRXA", "dataElementForAnelgesicsCategory.json");
        setUpSuccessHttpGetRequest("/api/categoryCombos/c4pX5PGlfRV", "categoryComboForAnelgesicsCategory.json");

        List<Category> categories = dhis2.fetchCommodities(new User("test", "pass"));

        assertThat(categories.size(), is(1));
        Category firstCategory = categories.get(0);
        assertThat(firstCategory.getName(), equalTo("Anelgesics"));
        List<Commodity> allCommodities = firstCategory.getNotSavedCommodities();
        assertThat(allCommodities.size(), equalTo(2));
        assertThat(allCommodities.get(0).getName(), equalTo("(Paracetamol_Tablet_125g)"));
        assertThat(allCommodities.get(1).getName(), equalTo("(Paracetamol_Drops_15ml)"));
    }

    @Test
    public void testShouldFetchReasonsForOrder() throws Exception {
        setUpSuccessHttpGetRequest("/api/systemSettings/reasons_for_order", "systemSettingForReasonsForOrder.json");

        Map<String, List<String>> reasons = dhis2.fetchOrderReasons(new User("test", "pass"));

        assertThat(reasons.size(), is(2));
        assertThat(reasons.get(ORDER_REASONS_JSON_KEY), contains("Emergency", "Routine"));
        assertThat(reasons.get(UNEXPECTED_QUANTITY_JSON_KEY), contains("High Demand", "Losses", "Expirations", "Adjustments"));
    }

    private void setUpSuccessHttpGetRequest(String uri, String fixtureFile) throws IOException {
        String rootDataSetJson = readFixtureFile(fixtureFile);
        addHttpResponseRule("GET", dhis2BaseUrl + uri, new TestHttpResponse(200, rootDataSetJson));
    }

    private String readFixtureFile(String fileName) throws IOException {
        URL url = this.getClass().getClassLoader().getResource("fixtures/" + fileName);
        InputStream src = url.openStream();
        String content = IOUtils.toString(src);
        src.close();
        return content;
    }
}
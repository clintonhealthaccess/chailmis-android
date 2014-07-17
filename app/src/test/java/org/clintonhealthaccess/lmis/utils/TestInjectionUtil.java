package org.clintonhealthaccess.lmis.utils;

import android.content.Context;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

import org.clintonhealthaccess.lmis.app.config.GuiceConfigurationModule;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import roboguice.inject.RoboInjector;

import static com.google.inject.util.Modules.override;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;
import static roboguice.RoboGuice.DEFAULT_STAGE;
import static roboguice.RoboGuice.getInjector;
import static roboguice.RoboGuice.newDefaultRoboModule;
import static roboguice.RoboGuice.setBaseApplicationInjector;

public class TestInjectionUtil {
    public static void setUpInjection(Object testCase, Module mockedModule) {
        Module customisedModule = override(newDefaultRoboModule(application)).with(new GuiceConfigurationModule());
        if (mockedModule != null) {
            customisedModule = override(customisedModule).with(mockedModule);
        }

        setBaseApplicationInjector(application, DEFAULT_STAGE, customisedModule);
        RoboInjector injector = getInjector(application);
        injector.injectMembersWithoutViews(testCase);
    }

    public static void setUpInjection(Object testCase) {
        setUpInjection(testCase, null);
    }

    public static void setUpInjectionWithMockLmisServer(Context context, Object testCase, Module anotherMockedModule) throws IOException {
        final LmisServer mockLmisServer = mock(LmisServer.class);
        when(mockLmisServer.fetchCommodities((User) anyObject())).thenReturn(defaultCategories(context));
        Module mockedModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(LmisServer.class).toInstance(mockLmisServer);
            }
        };
        if(anotherMockedModule != null) {
            mockedModule = override(mockedModule).with(anotherMockedModule);
        }
        setUpInjection(testCase, mockedModule);
    }

    public static void setUpInjectionWithMockLmisServer(Context context, Object testCase) throws IOException {
        setUpInjectionWithMockLmisServer(context, testCase, null);
    }

    private static List<Category> defaultCategories(Context context) throws IOException {
        InputStream src = context.getAssets().open("default_commodities.json");
        String defaultCommoditiesAsJson = CharStreams.toString(new InputStreamReader(src));
        return asList(new Gson().fromJson(defaultCommoditiesAsJson, Category[].class));
    }
}

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

package org.clintonhealthaccess.lmis.utils;

import android.content.Context;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

import org.apache.commons.io.IOUtils;
import org.clintonhealthaccess.lmis.app.config.GuiceConfigurationModule;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.api.DataValueSet;
import org.clintonhealthaccess.lmis.app.models.api.DataValueSetPushResponse;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<Category> categories = defaultCategories(context);
        when(mockLmisServer.fetchCommodities((User) anyObject())).thenReturn(categories);
        when(mockLmisServer.fetchStockLevels((List<Commodity>) anyObject(), (User) anyObject())).thenReturn(testStockLevels(categories));
        when(mockLmisServer.pushDataValueSet((DataValueSet) anyObject(), (User) anyObject())).thenReturn(fakePushDataValuesResponse());
        Module mockedModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(LmisServer.class).toInstance(mockLmisServer);
            }
        };
        if (anotherMockedModule != null) {
            mockedModule = override(mockedModule).with(anotherMockedModule);
        }
        setUpInjection(testCase, mockedModule);
    }

    private static DataValueSetPushResponse fakePushDataValuesResponse() {
        try {
            String json = readFixtureFile("successfulSnapshotPush.json");
            return new Gson().fromJson(json, DataValueSetPushResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Map<Commodity, Integer> testStockLevels(List<Category> categories) {
        HashMap<Commodity, Integer> result = new HashMap<>();
        for (Category category : categories) {
            for (Commodity commodity : category.getNotSavedCommodities()) {
                result.put(commodity, 10);
            }

        }
        return result;
    }

    public static void setUpInjectionWithMockLmisServer(Context context, Object testCase) throws IOException {
        setUpInjectionWithMockLmisServer(context, testCase, null);
    }

    private static List<Category> defaultCategories(Context context) throws IOException {
        InputStream src = context.getAssets().open("default_commodities.json");
        String defaultCommoditiesAsJson = CharStreams.toString(new InputStreamReader(src));
        return asList(new Gson().fromJson(defaultCommoditiesAsJson, Category[].class));
    }

    private static String readFixtureFile(String fileName) throws IOException {
        URL url = TestInjectionUtil.class.getClassLoader().getResource("fixtures/" + fileName);
        InputStream src = url.openStream();
        String content = IOUtils.toString(src);
        src.close();
        return content;
    }
}

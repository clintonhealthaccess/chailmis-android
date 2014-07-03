package org.clintonhealthaccess.lmis.utils;

import com.google.inject.AbstractModule;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

import static org.robolectric.Robolectric.application;
import static roboguice.RoboGuice.DEFAULT_STAGE;
import static roboguice.RoboGuice.newDefaultRoboModule;
import static roboguice.RoboGuice.setBaseApplicationInjector;

public class TestInjectionUtil {
    public static void setUpInjection(Object testCase, AbstractModule mockedModule) {
        if (mockedModule != null) {
            setBaseApplicationInjector(application, DEFAULT_STAGE, newDefaultRoboModule(application), mockedModule);
        }
        RoboInjector injector = RoboGuice.getInjector(application);
        injector.injectMembersWithoutViews(testCase);
    }

    public static void setUpInjection(Object testCase) {
        setUpInjection(testCase, null);
    }
}

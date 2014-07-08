package org.clintonhealthaccess.lmis.utils;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import org.clintonhealthaccess.lmis.app.config.GuiceConfigurationModule;

import roboguice.inject.RoboInjector;

import static com.google.inject.util.Modules.override;
import static org.robolectric.Robolectric.application;
import static roboguice.RoboGuice.DEFAULT_STAGE;
import static roboguice.RoboGuice.getInjector;
import static roboguice.RoboGuice.newDefaultRoboModule;
import static roboguice.RoboGuice.setBaseApplicationInjector;

public class TestInjectionUtil {
    public static void setUpInjection(Object testCase, AbstractModule mockedModule) {
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
}

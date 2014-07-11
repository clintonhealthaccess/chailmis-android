package org.clintonhealthaccess.lmis.app;

import android.app.Application;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.config.GuiceConfigurationModule;
import org.clintonhealthaccess.lmis.app.services.CategoryService;

import static roboguice.RoboGuice.DEFAULT_STAGE;
import static roboguice.RoboGuice.getInjector;
import static roboguice.RoboGuice.newDefaultRoboModule;
import static roboguice.RoboGuice.setBaseApplicationInjector;

public class LmisApplication extends Application {
    @Inject
    private CategoryService categoryService;

    @Override
    public void onCreate() {
        super.onCreate();
        setBaseApplicationInjector(this, DEFAULT_STAGE, newDefaultRoboModule(this), new GuiceConfigurationModule());

        // load all categories to warm up
        getInjector(this).injectMembersWithoutViews(this);
        categoryService.all();
    }
}

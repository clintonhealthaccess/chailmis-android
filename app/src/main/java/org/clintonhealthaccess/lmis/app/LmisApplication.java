package org.clintonhealthaccess.lmis.app;

import android.app.Application;

import org.clintonhealthaccess.lmis.app.config.GuiceConfigurationModule;

import static roboguice.RoboGuice.DEFAULT_STAGE;
import static roboguice.RoboGuice.newDefaultRoboModule;
import static roboguice.RoboGuice.setBaseApplicationInjector;

public class LmisApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setBaseApplicationInjector(this, DEFAULT_STAGE, newDefaultRoboModule(this), new GuiceConfigurationModule());
    }
}

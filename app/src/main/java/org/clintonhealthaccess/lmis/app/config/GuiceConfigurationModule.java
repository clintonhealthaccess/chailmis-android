package org.clintonhealthaccess.lmis.app.config;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.remote.Dhis2;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

public class GuiceConfigurationModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(LmisServer.class).to(Dhis2.class);
    }
}

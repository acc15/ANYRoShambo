package com.appctek.anyroshambo;

import com.appctek.anyroshambo.services.AdService;
import com.appctek.anyroshambo.services.NoAdService;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-11-01
 */
public class AppModule extends AbstractModule {

    public static final String AD_SERVICE_CLASS = "com.appctek.anyroshambo.services.StartAppAdService";

    @Override
    protected void configure() {
        bind(AppInfo.class).toInstance(new AppInfo(AppModule.class));
        try {
            bind(AdService.class).to(Class.forName(AD_SERVICE_CLASS).asSubclass(AdService.class)).in(Singleton.class);
        } catch (ClassNotFoundException e) {
            bind(AdService.class).to(NoAdService.class).in(Singleton.class);
        }
    }
}

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

    private static Class<?> getClassOrDefault(String className, Class<?> defaultClass) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return defaultClass;
        }
    }

    @Override
    protected void configure() {
        bind(AppInfo.class).toInstance(new AppInfo(AppModule.class));
        bind(AdService.class).to(getClassOrDefault(AD_SERVICE_CLASS, NoAdService.class).
                asSubclass(AdService.class)).in(Singleton.class);
    }
}

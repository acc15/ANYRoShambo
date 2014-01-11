package com.appctek.anyroshambo;

import com.appctek.anyroshambo.services.AdService;
import com.appctek.anyroshambo.services.StartAppAdService;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-11-01
 */
public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        final AppInfo appInfo = new AppInfo(AppModule.class);
        bind(AppInfo.class).toInstance(appInfo);
        bind(AdService.class).to(StartAppAdService.class).in(Singleton.class);
    }
}

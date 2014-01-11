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
    @Override
    protected void configure() {
        bind(AppInfo.class).toInstance(new AppInfo(AppModule.class));
        bind(AdService.class).to(NoAdService.class).in(Singleton.class);
    }
}

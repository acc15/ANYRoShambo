package com.appctek.anyroshambo;

import com.google.inject.AbstractModule;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-11-01
 */
public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        final AppInfo appInfo = new AppInfo(AppModule.class);
        bind(AppInfo.class).toInstance(appInfo);
    }
}

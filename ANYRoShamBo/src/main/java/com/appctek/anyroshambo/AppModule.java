package com.appctek.anyroshambo;

import com.appctek.anyroshambo.services.AdService;
import com.appctek.anyroshambo.services.NoAdService;
import com.appctek.anyroshambo.social.OdnoklassnikiService;
import com.appctek.anyroshambo.social.SocialNetworkService;
import com.appctek.anyroshambo.social.TwitterService;
import com.appctek.anyroshambo.social.VkontakteService;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-11-01
 */
public class AppModule extends AbstractModule {

    private static Class<?> getAdServiceImplClass() {
        if (AppBuild.AD_ENABLED) {
            try {
                return Class.forName("com.appctek.anyroshambo.services.StartAppAdService");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Can't find AD service class", e);
            }
        } else {
            return NoAdService.class;
        }
    }

    @Override
    protected void configure() {
        bind(AdService.class).to(getAdServiceImplClass().asSubclass(AdService.class)).in(Singleton.class);
        bind(SocialNetworkService.class).annotatedWith(Names.named("vkService")).to(VkontakteService.class);
        bind(SocialNetworkService.class).annotatedWith(Names.named("okService")).to(OdnoklassnikiService.class);
        bind(SocialNetworkService.class).annotatedWith(Names.named("twService")).to(TwitterService.class);
        bind(HttpClient.class).to(DefaultHttpClient.class);
    }
}

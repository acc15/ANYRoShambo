package com.appctek.anyroshambo;

import com.appctek.anyroshambo.services.AdService;
import com.appctek.anyroshambo.services.NoAdService;
import com.appctek.anyroshambo.social.OdnoklassnikiService;
import com.appctek.anyroshambo.social.SocialNetworkService;
import com.appctek.anyroshambo.social.TwitterService;
import com.appctek.anyroshambo.social.VkontakteService;
import com.appctek.anyroshambo.social.auth.OAuthToken;
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

        bindConstant().annotatedWith(Names.named("shareLink")).to("http://appctek.com/");

        bindConstant().annotatedWith(Names.named("okAppId")).to("216398080");
        bindConstant().annotatedWith(Names.named("okPublicKey")).to("CBAKPGONABABABABA");
        bindConstant().annotatedWith(Names.named("okSecretKey")).to("83B562785858040AF1E5DF41");
        bindConstant().annotatedWith(Names.named("okRedirectUri")).to("http://appctek.com/anyroshambo");

        bindConstant().annotatedWith(Names.named("vkAppId")).to("4087551");

        bind(OAuthToken.class).annotatedWith(Names.named("twConsumerToken")).toInstance(new OAuthToken(
                "eNAz8lhCSpXog4F0UbScQA", "KbVJTv5BlHMJlETCARFWwBHlTq53wim7XJocuG5N5So"));
        bindConstant().annotatedWith(Names.named("twRedirectUri")).to("http://appctek.com/anyroshambo");



    }
}

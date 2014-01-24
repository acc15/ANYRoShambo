package com.appctek.anyroshambo.social;

import com.appctek.anyroshambo.social.auth.ErrorInfo;
import com.appctek.anyroshambo.util.Action;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-29-12
 */
public interface SocialNetworkService {

    public static enum CommonError {
        USER_CANCELLED,
        AUTH_ERROR
    }

    void shareText(boolean revoke, String text, Action<ErrorInfo> errorHandler);
}

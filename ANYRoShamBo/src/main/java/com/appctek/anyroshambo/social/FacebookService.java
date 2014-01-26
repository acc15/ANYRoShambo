package com.appctek.anyroshambo.social;

import android.app.Activity;
import android.os.Bundle;
import com.appctek.anyroshambo.roboguice.OnSaveInstanceStateEvent;
import com.appctek.anyroshambo.social.auth.ErrorInfo;
import com.facebook.*;
import com.facebook.widget.WebDialog;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roboguice.activity.event.*;
import roboguice.event.Observes;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-25-01
 */
public class FacebookService implements SocialNetworkService {

    private static final Logger logger = LoggerFactory.getLogger(FacebookService.class);

    private Activity activity;

    private UiLifecycleHelper uiHelper;

    public static enum Error {
        FEED_ERROR
    }

    @Inject
    public FacebookService(Activity activity) {
        this.activity = activity;
        this.uiHelper = new UiLifecycleHelper(activity, null);
    }

    public void onCreate(@Observes OnCreateEvent createEvent) {
        uiHelper.onCreate(createEvent.getSavedInstanceState());
    }

    public void onActivityResult(@Observes OnActivityResultEvent event) {
        uiHelper.onActivityResult(event.getRequestCode(), event.getResultCode(), event.getData());
    }

    public void onResume(@Observes OnResumeEvent event) {
        uiHelper.onResume();
    }

    public void onSaveInstanceState(@Observes OnSaveInstanceStateEvent event) {
        uiHelper.onSaveInstanceState(event.getOutBundle());
    }

    public void onPause(@Observes OnPauseEvent event) {
        uiHelper.onPause();
    }

    public void onDestroy(@Observes OnDestroyEvent event) {
        uiHelper.onDestroy();
    }

    private ErrorInfo createErrorInfoByException(Enum<?> defaultErrorCode, Exception ex) {
        return ErrorInfo.create(ex instanceof FacebookOperationCanceledException
                ? CommonError.USER_CANCELLED
                : defaultErrorCode).withThrowable(ex);
    }

    public void share(final ShareParams shareParams) {
        Session.openActiveSession(activity, true, new Session.StatusCallback() {
            public void call(Session session, SessionState state, Exception exception) {

                if (exception != null) {
                    logger.error("FacebookService error occurred while authenticating", exception);
                    shareParams.getFinishAction().execute(
                            createErrorInfoByException(CommonError.AUTH_ERROR, exception));
                    return;
                }

                if (session.isOpened()) {

                    final Bundle params = new Bundle();
                    params.putString("name", shareParams.getTitle());
                    params.putString("description", shareParams.getText());
                    params.putString("link", shareParams.getLink());
                    new WebDialog.FeedDialogBuilder(activity, session, params).
                            setOnCompleteListener(new WebDialog.OnCompleteListener() {
                                public void onComplete(Bundle values, FacebookException error) {
                                    final ErrorInfo errorInfo;
                                    if (error == null) {
                                        final String postId = values.getString("post_id");
                                        errorInfo = postId != null
                                                ? ErrorInfo.success()
                                                : ErrorInfo.create(CommonError.USER_CANCELLED);
                                    } else {
                                        errorInfo = createErrorInfoByException(Error.FEED_ERROR, error);
                                    }
                                    shareParams.getFinishAction().execute(errorInfo);
                                }
                            }).build().show();
                }
            }
        });

    }
}

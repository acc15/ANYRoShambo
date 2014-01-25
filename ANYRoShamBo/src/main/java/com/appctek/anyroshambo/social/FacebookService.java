package com.appctek.anyroshambo.social;

import android.app.Activity;
import android.os.Bundle;
import com.appctek.anyroshambo.roboguice.OnSaveInstanceStateEvent;
import com.appctek.anyroshambo.social.auth.ErrorInfo;
import com.appctek.anyroshambo.util.Action;
import com.facebook.*;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.google.inject.Inject;
import roboguice.activity.event.*;
import roboguice.event.Observes;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-25-01
 */
public class FacebookService implements SocialNetworkService {

    private Activity activity;

    private UiLifecycleHelper uiHelper;

    private Action<ErrorInfo> lastFinishAction; // state for tracking last share call result

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
        uiHelper.onActivityResult(event.getRequestCode(), event.getResultCode(), event.getData(), new FacebookDialog.Callback() {
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                if (lastFinishAction != null) {
                    lastFinishAction.execute(ErrorInfo.success());
                    lastFinishAction = null;
                }
            }

            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                if (lastFinishAction == null) {
                    return;
                }
                lastFinishAction.execute(ErrorInfo.create(error instanceof FacebookOperationCanceledException
                        ? CommonError.USER_CANCELLED
                        : Error.FEED_ERROR).
                        withThrowable(error));
                lastFinishAction = null;
            }
        });
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

    public void share(final ShareParams shareParams) {
        Session.openActiveSession(activity, true, new Session.StatusCallback() {
            public void call(Session session, SessionState state, Exception exception) {
                if (session.isOpened()) {

                    if (FacebookDialog.canPresentShareDialog(activity)) {
                        final FacebookDialog.PendingCall pendingCall = new FacebookDialog.ShareDialogBuilder(activity).
                                setName(shareParams.getTitle()).
                                setLink(shareParams.getLink()).
                                setDescription(shareParams.getText()).
                                build().present();
                        lastFinishAction = shareParams.getFinishAction();
                        uiHelper.trackPendingDialogCall(pendingCall);
                        return;
                    }

                    final Bundle params = new Bundle();
                    params.putString("name", shareParams.getTitle());
                    params.putString("description", shareParams.getText());
                    params.putString("link", shareParams.getLink());
                    new WebDialog.FeedDialogBuilder(activity, session, params).
                            setOnCompleteListener(new WebDialog.OnCompleteListener() {
                                public void onComplete(Bundle values,
                                                       FacebookException error) {
                                    final ErrorInfo errorInfo;
                                    if (error == null) {
                                        final String postId = values.getString("post_id");
                                        errorInfo = postId != null
                                                ? ErrorInfo.success().withDetail("post_id", postId)
                                                : ErrorInfo.create(CommonError.USER_CANCELLED);
                                    } else if (error instanceof FacebookOperationCanceledException) {
                                        errorInfo = ErrorInfo.create(CommonError.USER_CANCELLED).withThrowable(error);
                                    } else {
                                        errorInfo = ErrorInfo.create(Error.FEED_ERROR).withThrowable(error);
                                    }
                                    shareParams.getFinishAction().execute(errorInfo);
                                }
                            }).build().show();
                }
            }
        });

    }
}

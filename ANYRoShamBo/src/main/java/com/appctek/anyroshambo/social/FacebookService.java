package com.appctek.anyroshambo.social;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import com.facebook.*;
import com.facebook.widget.WebDialog;
import com.google.inject.Inject;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-25-01
 */
public class FacebookService implements SocialNetworkService {

    private Activity activity;

    @Inject
    public FacebookService(Activity activity) {
        this.activity = activity;
    }

    public void share(final ShareParams shareParams) {
        Session.openActiveSession(activity, true, new Session.StatusCallback() {
            public void call(Session session, SessionState state, Exception exception) {
                if (session.isOpened()) {
                    final Bundle params = new Bundle();
                    params.putString("name", "Facebook SDK for Android");
                    params.putString("caption", "Build great social apps and get more installs.");
                    params.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
                    params.putString("link", "https://developers.facebook.com/android");
                    params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
                    new WebDialog.FeedDialogBuilder(activity, session, params).
                            setOnCompleteListener(new WebDialog.OnCompleteListener() {
                                public void onComplete(Bundle values,
                                                       FacebookException error) {
                                    if (error == null) {
                                        final String postId = values.getString("post_id");
                                        if (postId != null) {
                                            Toast.makeText(activity, "Posted story, id: " + postId, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(activity, "Publish cancelled", Toast.LENGTH_SHORT).show();
                                        }
                                    } else if (error instanceof FacebookOperationCanceledException) {
                                        Toast.makeText(activity, "Publish cancelled", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(activity, "Error posting story", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).build().show();
                }
            }
        });

    }
}

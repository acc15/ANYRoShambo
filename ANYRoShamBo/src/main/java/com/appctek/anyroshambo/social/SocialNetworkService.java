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

    public static class ShareParams {

        private String title;
        private String text;
        private String link;
        private boolean revoke = false;
        private Action<ErrorInfo> finishAction = null;

        public ShareParams() {
        }

        public ShareParams revoke(boolean revoke) {
            this.revoke = revoke;
            return this;
        }

        public ShareParams onFinish(Action<ErrorInfo> action) {
            this.finishAction = action;
            return this;
        }

        public ShareParams title(String title) {
            this.title = title;
            return this;
        }

        public ShareParams text(String text) {
            this.text = text;
            return this;
        }

        public ShareParams link(String url) {
            this.link = url;
            return this;
        }

        public boolean doRevoke() {
            return revoke;
        }

        public String getText() {
            return text;
        }

        public String getTitle() { return title; }

        public String getLink() {
            return link;
        }

        public Action<ErrorInfo> getFinishAction() {
            return finishAction;
        }

        public void invokeFinishAction(ErrorInfo errorInfo) {
            if (finishAction == null) {
                return;
            }
            finishAction.execute(errorInfo);
        }
    }

    void share(ShareParams shareParams);
}

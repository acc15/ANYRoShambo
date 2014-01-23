package com.appctek.anyroshambo.social.task;

import android.os.AsyncTask;
import android.view.View;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public class TaskManager {

    private View context;

    public <P,R> void executeAsync(final Task<P,R> task, final P param) {
        new AsyncTask<Object, Object, R>() {
            @SuppressWarnings("unchecked")
            @Override
            protected R doInBackground(Object... params) {
                return task.execute((P)params[0]);
            }

            @Override
            protected void onPostExecute(R r) {
                task.onFinish(r);
            }
        }.execute(param);
    }

    public <P,R> void executeFinish(final Task<P,R> task, final R result) {
        context.post(new Runnable() {
            public void run() {
                task.onFinish(result);
            }
        });
    }

    public <P,R> void execute(final Task<P,R> task, final P param) {
        task.onFinish(task.execute(param));
    }

}

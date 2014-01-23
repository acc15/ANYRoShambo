package com.appctek.anyroshambo.social.task;

import android.os.AsyncTask;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public class TaskManager {

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

    public <P,R> void execute(final Task<P,R> task, final P param) {
        task.onFinish(task.execute(param));
    }

}

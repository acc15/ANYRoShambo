package com.appctek.anyroshambo.services;

/**
 * For testing purposes. To allow mocking of current time
 * @author Vyacheslav Mayorov
 * @since 2013-23-12
 */
public class DateTimeService {

    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

}

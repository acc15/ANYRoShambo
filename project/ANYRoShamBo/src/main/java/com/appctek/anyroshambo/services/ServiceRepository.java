package com.appctek.anyroshambo.services;

import android.content.Context;
import com.appctek.anyroshambo.FadedSequentialAnimator;
import com.appctek.anyroshambo.ShakeDetector;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-23-12
 */
public class ServiceRepository {


    public FadedSequentialAnimator getSequentialAnimator() {
        return new FadedSequentialAnimator();
    }

    private static class DefaultServiceRepositoryFactory implements ServiceRepositoryFactory {
        private static ServiceRepository repository = new ServiceRepository();
        public ServiceRepository getServiceRepository() {
            return repository;
        }
    }

    private static ServiceRepositoryFactory findRepositoryFactory() {
        final String overriddenFactory = System.getProperty(ServiceRepositoryFactory.class.getName());
        if (overriddenFactory != null) {
            try {
                return (ServiceRepositoryFactory) Class.forName(overriddenFactory).newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return new DefaultServiceRepositoryFactory();
    }

    private static final ServiceRepositoryFactory repositoryFactory = findRepositoryFactory();

    public static ServiceRepository getRepository() {
        return repositoryFactory.getServiceRepository();
    }


    private DateTimeService dateTimeService = new DateTimeService();

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public ShakeDetector getShakeDetector(Context context) {
        return new ShakeDetector(context, getDateTimeService());
    }



}

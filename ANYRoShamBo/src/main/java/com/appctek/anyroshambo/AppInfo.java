package com.appctek.anyroshambo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-04-01
 */
public class AppInfo {

    private static final Logger logger = LoggerFactory.getLogger(AppInfo.class);

    public static final String APP_PROPERTIES = "app.properties";
    private final String appPackage;

    public AppInfo(Class<?> appClass) {
        this.appPackage = appClass.getPackage().getName();
        final InputStream inputStream = appClass.getResourceAsStream(APP_PROPERTIES);
        if (inputStream == null) {
            logger.error("Can't find " + APP_PROPERTIES + " resource");
            return;
        }
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            logger.error("Can't parse " + APP_PROPERTIES + " file", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("Can't close " + APP_PROPERTIES + " input stream", e);
            }
        }
    }

    private Properties properties = new Properties();

    private String getAppProperty(String suffix) {
        return properties.getProperty(appPackage + "." + suffix);
    }

    public String getVersion() {
        return getAppProperty("version");
    }

    public boolean isDebug() {
        return Boolean.parseBoolean(getAppProperty("debug"));
    }
}

package org.monitis.logmonitor.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Config {

    protected static final String CONFIG_FILENAME = "logmonitor.properties";
    private static final Properties props = new Properties();

    static {
        try {
            loadConfig(null);
        } catch (Exception ex) {
            System.err.println("Couldn't load config file");
        }
//        printConfig();
    }

    private Config() {
    }

    /**
     * NOTE: Unnecessary to call this function directly because 
     *       it is already called during class initialization
     * @param propertiesRootPath
     * @throws Exception
     */
    public static synchronized void loadConfig(String propertiesRootPath) throws Exception {
        if (propertiesRootPath == null || propertiesRootPath.length() <= 0) {
            propertiesRootPath = System.getProperty("mon.properties.root.path", "properties");
        }
        if (new File(propertiesRootPath).exists() == false) {
            propertiesRootPath = "../" + propertiesRootPath;
            if (new File(propertiesRootPath).exists() == false) {
                propertiesRootPath = "../" + propertiesRootPath;
                if (new File(propertiesRootPath).exists() == false) {
                    throw new Exception("Couldn't find properties file location");
                }
            }
        }
        FileInputStream stream = new FileInputStream(new File(propertiesRootPath, CONFIG_FILENAME));
        props.load(stream);
        stream.close();
    }

    public static synchronized void printConfig() {

        String header = "********** Config ************";
        String msg;
        StringBuffer buf = new StringBuffer();
        buf.append(header).append("\n");
        Enumeration keys = props.propertyNames();
        while (keys.hasMoreElements()) {
            msg = (String) keys.nextElement();
            buf.append(msg).append("=").append(props.getProperty(msg)).append("\n");
        }
        buf.append("********************").append("\n");
        msg = buf.toString();
        Logger logger = Logger.getLogger("Config");
        if (logger != null) {
            logger.info(msg);

        } else {
            System.out.println(msg);
        }
    }

    public static String getConfigStringValue(String key) {
        return props.getProperty(key);
    }

    public static int getConfigIntValue(String key) {
        return Integer.parseInt(props.getProperty(key, "0"));
    }

    public static String getConfigStringValue(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public static int getConfigIntValue(String key, int defaultValue) {
        return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
    }

    public static int getConfigIntValue(String key, int minValue, int maxValue) {
        int value = getConfigIntValue(key);
        if (value < minValue) {
            return minValue;
        }
        if (value > maxValue) {
            return maxValue;
        }
        return value;
    }

    public static boolean getConfigBooleanValue(String key) {
        try {
            //First check on numeric value
            int val = getConfigIntValue(key);
            if (val != 0) {
                return true;
            }
        } catch (NumberFormatException ex) {
            //Now check on string value
            String str = props.getProperty(key);
            if (str != null
                    && (str.compareToIgnoreCase("true") == 0
                    || str.compareToIgnoreCase("yes") == 0
                    || str.compareToIgnoreCase("on") == 0)) {
                return true;
            }
        }
        return false;
    }
}

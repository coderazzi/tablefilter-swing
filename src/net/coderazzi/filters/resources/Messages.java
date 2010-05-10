package net.coderazzi.filters.resources;

import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class Messages {
    private static final String BUNDLE_NAME = "net.coderazzi.filters.resources.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
    }

    public static String getString(String key) {
    	String ret = getString(key, null);
    	if (ret==null){
            ret = '!' + key + '!';
    	}
    	return ret;
    }

    public static String getString(String key, String defaultValue) {
    	String ret = System.getProperty("net.coderazzi.filters."+key);
    	if (ret==null){
	        try {
	            ret = RESOURCE_BUNDLE.getString(key);
	        } catch (MissingResourceException e) {
	            ret = defaultValue;
	        }
    	}
    	return ret;
    }
}

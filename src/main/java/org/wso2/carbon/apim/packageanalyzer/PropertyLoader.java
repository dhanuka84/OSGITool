package org.wso2.carbon.apim.packageanalyzer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyLoader {
	private static final Log log = LogFactory.getLog(PropertyLoader.class);

	private static final String PROPERTY_FILE_PATH = "data.properties";
	private static Properties prop = null;

	private static final PropertyLoader loader = new PropertyLoader();

	private PropertyLoader() {
		/*init();*/
	}

	/*
	 * public static PropertyLoader getInstance() { return loader; }
	 */

	/**
	 * Load property file data
	 */
	public static void init() {
		try {
			prop = FileHandler.loadResourceProperties(PROPERTY_FILE_PATH);
		} catch (FileNotFoundException e) {
			String msg = "Error occured while loading property file";
			log.error(msg, e);
			throw new APIMRuntimException(msg, e);
		} catch (IOException e) {
			String msg = "Error occured while reading property file";
			log.error(msg, e);
			throw new APIMRuntimException(msg, e);
		}
	}

	/**
	 * Return the property value of a given property name
	 * 
	 * @param propertyName
	 *            property name
	 * @return property value
	 */
	public static String getPropertyValue(String propertyName) {
		String value = prop.getProperty(propertyName).trim();
		if (value == null) {
			String msg = "No value find for the property " + propertyName;
			log.error(msg);
			throw new APIMRuntimException(msg);
		}
		return value;
	}

}

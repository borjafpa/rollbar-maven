package com.mhlopko.rollbar.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


public class AppConfiguration {

	public static final String DEFAULT_FILE = "rollbar.properties";
	private static Properties appProperties = null;
	private static String configurationFile = DEFAULT_FILE;

	/**
	 * Get the value for a property into the properties file passing its name
	 * @param key Property name
	 * @return Value for the property
	 */
	public static String get(String key) {

		if ( appProperties == null ) {
			try {

				appProperties = new Properties();
				appProperties.load(
						AppConfiguration.class.getClassLoader().
							getResourceAsStream(configurationFile)
				);

			} catch (FileNotFoundException e) {
				throw new RuntimeException(
						"Could not find the configuration file: " + configurationFile
				);
			} catch (IOException e) {
				throw new RuntimeException(
						"Could not read the configuration file: " + configurationFile
				);
			}

		}

		return appProperties.getProperty(key);
	}

	/**
	 * Set the configuration file name
	 * @param Configuration file name
	 */
	public static void setConfigurationFilePath(String file) {
		configurationFile = file;
	}

	/**
	 * Get the configuration file name
	 * @return Configuration file name
	 */
	public static String getConfigurationFile() {
		return configurationFile;
	}
}

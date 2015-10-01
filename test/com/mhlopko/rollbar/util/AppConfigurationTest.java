package com.mhlopko.rollbar.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AppConfigurationTest {

	@Test
	public void testGetConfigurationFile() {
		String appFile = "rollbar.properties";

		assertEquals(
					"The App Configuration path should be the original",
					appFile,
					AppConfiguration.getConfigurationFile()
					);
	}

	@Test
	public void testSetConfigurationFile() {
		String originalAppFile = "rollbar.properties";
		String newAppFile = "test_rollbar.properties";

		assertEquals(
					"The App Configuration path should be the original",
					originalAppFile,
					AppConfiguration.getConfigurationFile()
					);

		AppConfiguration.setConfigurationFilePath(newAppFile);

		assertEquals(
					"The App Configuration path should be the new",
					newAppFile,
					AppConfiguration.getConfigurationFile()
				);

		AppConfiguration.setConfigurationFilePath(originalAppFile);
	}

	@Test
	public void testGet() {
		assertEquals(
					"The 'API_KEY' value should be '45be8a5a8f8a402db2dfd13e21ca85e1'",
					"45be8a5a8f8a402db2dfd13e21ca85e1",
					AppConfiguration.get("API_KEY")
					);
	}
}

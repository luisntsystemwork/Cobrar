package com.navicon.ws.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.ws.rs.core.Application;

public class JerseyConfig extends Application {

	public static final String PROPERTIES_FILE = "config.properties";
	public static Properties properties = new Properties();
	static {
		InputStream inputStream = JerseyConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
		if (inputStream != null) {
			try {
				properties.load(inputStream);
			} catch (IOException e) {
				// TODO Add your custom fail-over code here
				e.printStackTrace();
			}
		}
	}

	

}
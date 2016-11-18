package com.navicon.ws.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.ws.rs.core.Application;

public class JerseyConfig extends Application {

	public static final String PROPERTIES_FILE = "config.properties";
	public static Properties properties = new Properties();
	static {
		/*InputStream inputStream = JerseyConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);*/
		String path = File.separator + "ServidorOXP";
		if (System.getProperty("user.dir").indexOf("ServidorOXP") != -1) {

            path = "C:\\ServidorOXP";        
		}
		
		try {
			InputStream inputStream = new FileInputStream(new File(path + File.separator + PROPERTIES_FILE));
		
			if (inputStream != null) {
				properties.load(inputStream);
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 catch (IOException e) {
			// TODO Add your custom fail-over code here
			e.printStackTrace();
		}
	}

	

}
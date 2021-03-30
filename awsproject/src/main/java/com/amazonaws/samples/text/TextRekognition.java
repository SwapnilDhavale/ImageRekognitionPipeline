package com.amazonaws.samples.text;

import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class TextRekognition {

	public static void main(String[] args) {
		PropertiesConfiguration config = null;
		try {
			config = new PropertiesConfiguration("/home/ec2-user/config.properties");
		} catch (ConfigurationException e) {
			System.out.println("Error: Place config.properties file in /home/ec2-user");
			try {
				config = new PropertiesConfiguration("/Users/swapnildhavale/Desktop/Jars/config.properties");
			} catch (ConfigurationException e1) {
				System.out.println(e1);
				
			}
		}

		ArrayList<String> imageQueue = new ArrayList<String>();
		ArrayList<String> imgtext = new ArrayList<String>();

		Operations op = new Operations(config.getProperty("sqsUrl").toString(),
				config.getProperty("bucketname").toString(),config.getProperty("filepath").toString());

		imageQueue = op.getImagesSQS();
		imgtext=op.textRekognition(imageQueue);
		op.generateOutput(imgtext);
	}

}

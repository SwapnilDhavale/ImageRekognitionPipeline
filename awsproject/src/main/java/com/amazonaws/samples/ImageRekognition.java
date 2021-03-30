package com.amazonaws.samples;

import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class ImageRekognition {

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

		ArrayList<String> cars = new ArrayList<String>();
		ArrayList<String> sqsimp = new ArrayList<String>();
		
		GetData gd = new GetData(config.getProperty("bucketname").toString(),config.getProperty("sqsUrl").toString());
		
		cars = gd.getImages();
		sqsimp = gd.performRekognition(cars);
		gd.performSqs(sqsimp);
		
	}

}

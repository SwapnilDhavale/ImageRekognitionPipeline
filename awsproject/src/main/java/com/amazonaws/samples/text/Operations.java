package com.amazonaws.samples.text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class Operations {
	ArrayList<String> imageQueue = new ArrayList<String>();
	ArrayList<String> imgtext = new ArrayList<String>();
	
	String sqsUrl;
	String bucketname;
	String filepath;
	
	public Operations(String sqsUrl, String bucketname, String filepath) {
		super();
		this.sqsUrl = sqsUrl;
		this.bucketname = bucketname;
		this.filepath = filepath;
	}

	public ArrayList<String> getImagesSQS() {
		String end = "";
		boolean flag = true;

		AmazonSQS sqs = AmazonSQSClientBuilder.standard().build();

		
		// String sqsUrl = "https://sqs.us-east-2.amazonaws.com/390238997221/CarQueue";

		while (flag) {
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(sqsUrl);
			List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
			for (Message message : messages) {

				end = message.getBody();
				if (end.equals("-1")) {
					flag = false;
					//System.out.println(end);
					sqs.deleteMessage(sqsUrl, message.getReceiptHandle());
					break;
				}
				imageQueue.add(end);
				sqs.deleteMessage(sqsUrl, message.getReceiptHandle());

			}

			//System.out.println();
		}
		System.out.println("Fetching images from S3 bucket");
		return imageQueue;
	}

	public ArrayList<String> textRekognition(ArrayList<String> imageQueue) {
		
		System.out.println("Performing Text Rekognition...");
		
		for (int k = 0; k < imageQueue.size(); k++) {
			String photo = imageQueue.get(k);
			String bucket = bucketname;
			String current = imageQueue.get(k)+": ";
			String value="";

			AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

			DetectTextRequest request = new DetectTextRequest()
					.withImage(new Image().withS3Object(new S3Object().withName(photo).withBucket(bucket)));

			try {
				DetectTextResult result = rekognitionClient.detectText(request);
				List<TextDetection> textDetections = result.getTextDetections();

				//System.out.println("Detected lines and words for " + photo);
				for (TextDetection text : textDetections) {

					value = value + text.getDetectedText() + " ";
					System.out.println("Detected: " + text.getDetectedText());
					//System.out.println();
					break;
				}
				if(!value.equals(""))
				imgtext.add(current+": "+value);
			} catch (AmazonRekognitionException e) {
				e.printStackTrace();
			}
			

		}
		return imgtext;

	}
	
	public void generateOutput(ArrayList<String> text) {
		
		try {
		      File myObj = new File(filepath);
		      if (myObj.createNewFile()) {
		        System.out.println("File created: " + myObj.getName());
		      } else {
		        System.out.println("File already exists..Cleaning File..");
		        PrintWriter writer = new PrintWriter(filepath);
		        writer.print("");
		        writer.close();
		      }
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		
		for (int i =0 ; i<text.size();i++) {
			try {
			      FileWriter myWriter = new FileWriter(filepath,true);
			      myWriter.write(text.get(i)+"\n");
			      myWriter.close();
			    } catch (IOException e) {
			      System.out.println("An error occurred.");
			      e.printStackTrace();
			    }
			
		}
		System.out.println("Successfully wrote to the file.");
	}

}

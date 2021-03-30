package com.amazonaws.samples;

import java.util.ArrayList;

import java.util.List;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class GetData {

	private String bucketname;
	String sqsUrl;

	public GetData(String bucketname, String sqsUrl) {
		super();
		this.bucketname = bucketname;
		this.sqsUrl = sqsUrl;
	}

	ArrayList<String> cars = new ArrayList<String>();
	ArrayList<String> sqsimp = new ArrayList<String>();

	public ArrayList<String> getImages() {
		final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
		ListObjectsV2Result res = s3.listObjectsV2(bucketname);
		List<S3ObjectSummary> objects = res.getObjectSummaries();
		for (S3ObjectSummary os : objects) {
			if (os.getKey().contains("jpg") || os.getKey().contains("png"))
				cars.add(os.getKey());
		}
		System.out.println("Fetching Images from S3..");
		return cars;
	}

	public ArrayList<String> performRekognition(ArrayList<String> cars) {

		System.out.println("Performiong Image Rekognition....");

		for (int i = 0; i < cars.size(); i++) {
			String photo = cars.get(i);
			String bucket = bucketname;

			AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
					.withRegion(Regions.US_EAST_1).build();

			DetectLabelsRequest request = new DetectLabelsRequest()
					.withImage(new Image().withS3Object(new S3Object().withName(photo).withBucket(bucket)))
					.withMaxLabels(10).withMinConfidence(75F);

			try {
				DetectLabelsResult result = rekognitionClient.detectLabels(request);
				List<Label> labels = result.getLabels();

				for (Label label : labels) {
					if (label.getName().equals("Car") && label.getConfidence() > 90) {
						sqsimp.add(photo);
					}

				}
			} catch (AmazonRekognitionException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}

		return sqsimp;
	}

	public void performSqs(ArrayList<String> sqsimp) {
		int i;
		AmazonSQS sqs = AmazonSQSClientBuilder.standard().build();
		sqsimp.add("-1");

		final SendMessageRequest sendMessageRequest = new SendMessageRequest();
		for (i = 0; i < sqsimp.size(); i++) {

			sendMessageRequest.withMessageBody(sqsimp.get(i));
			sendMessageRequest.withQueueUrl(sqsUrl);

			sendMessageRequest.withMessageGroupId("Part1");
			sendMessageRequest.withMessageDeduplicationId(String.valueOf(i));
			sqs.sendMessage(sendMessageRequest);

		}
		System.out.println("Image Send to SQS..");

	}

}

package com.mscatdk.pubsub;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;

/**
 * Simulate a temperature sensor i a specific room
 *
 */
public class App {
	
	@Parameter(names= {"-p", "--projectID"}, description = "Google Cloud Platform Project ID")
	private String projectId;
	
	@Parameter(names= {"-t", "--topic"}, description = "PubSub Topic ID")
	private String topicId;
	
	@Parameter(names= {"-r", "--room"}, description = "Room name")
	private String roomName;

	@Parameter(names= "--period", description = "Approx. time between readings in ms")
	private Long period;
	
	@Parameter(names= "--temp", description = "Temperature")
	private Integer temp;

	private static final Random randomGenerator = new Random();
	
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	private static final Logger console = LoggerFactory.getLogger("console");
	
    public static void main( String[] args ) throws Exception {
        App app = new App();
        JCommander jc = JCommander.newBuilder()
        					.addObject(app)
        					.build();
        
        jc.parse(args);
        jc.setProgramName("Temperature-sensor");
        
        app.run();
    }
    
    public void run() throws Exception {
    	ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
    	Publisher publisher = null;
    	ExecutorService executer = Executors.newFixedThreadPool(10);
    	
    	try {
    		publisher = Publisher.newBuilder(topicName).build();
    		
    		if (period != null) {
    			simulate(publisher, executer);
    		} else {
    			publishReading(publisher, executer, temp);
    		}
    	} finally {
    		if (publisher != null) {
    			publisher.shutdown();
    		}
    		
    		executer.shutdownNow();
    	}
    }

	private void simulate(Publisher publisher, ExecutorService executer) {
		while (true) {
			int temperatur = 25 + randomGenerator.nextInt(20)-9;
			publishReading(publisher, executer, temperatur);
			
			sleep(period);
		}
	}

	private void publishReading(Publisher publisher, Executor executer, int temperature) {
		JsonObject json = new JsonObject();
		json.addProperty("room", roomName);
		json.addProperty("country", "DK");
		json.addProperty("temperature", temperature);
		
		String str = json.toString();
		ByteString data = ByteString.copyFromUtf8(str);
		
		console.info("Send message: {}", str);
		
		PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
										.setData(data)
										.putAttributes("ts", Long.toString(DateTime.now().getMillis()))
										.build();
		ApiFuture<String> future = publisher.publish(pubsubMessage);
		
		ApiFutures.addCallback(future, new ApiFutureCallback<String>() {
		   public void onSuccess(String messageId) {
			  console.info("published with message id: {}", messageId);
		   }

		   public void onFailure(Throwable t) {
			 console.info("failed to publish!", t);
		   }
		 }, executer);
	}
	

    
    private void sleep(Long millies) {
    	try {
    		Thread.sleep(millies);
    	} catch (Exception e) {
    		logger.error("Sleep was interrupted!", e);
    	}
    }
}

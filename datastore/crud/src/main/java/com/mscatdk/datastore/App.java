package com.mscatdk.datastore;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.exception;

import java.io.IOException;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.datastore.Datastore;
import com.mscatdk.datastore.api.CustomerAPI;
import com.mscatdk.datastore.dao.CustomerDAO;
import com.mscatdk.datastore.dao.DatastoreFactory;

/**
 * CRUD operations
 *
 */
public class App  {
	
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	private static final String CUSTOMER_BASE_URL = "/customer";
	private static final String CUSTOMER_ENTITY_URL = CUSTOMER_BASE_URL + "/" + CustomerAPI.ID_PARAM_NAME;
	
    public static void main( String[] args ) throws IOException {
    	logger.info("Starting Application...");
    	AppConfig appConfig = ConfigFactory.create(AppConfig.class, System.getProperties());
		
		Datastore datastore = DatastoreFactory.getInstance(appConfig);

    	CustomerDAO customerDAO = new CustomerDAO(datastore);
    	CustomerAPI customerAPI = new CustomerAPI(customerDAO);
    	
    	logger.info("Project id: {}", appConfig.projectId());
    	logger.info("Credential Path: {}", appConfig.credentialPath());
    	logger.info("Namespace: {}", appConfig.namespace());
    	
    	get(CUSTOMER_BASE_URL, customerAPI.handleCustomerList, new JsonTransformer());
    	post(CUSTOMER_BASE_URL, customerAPI.handleCustomerCreate, new JsonTransformer());
    	
    	get(CUSTOMER_ENTITY_URL, customerAPI.handleCustomerGet, new JsonTransformer());
    	put(CUSTOMER_ENTITY_URL, customerAPI.handleCustomerUpdate, new JsonTransformer());
    	delete(CUSTOMER_ENTITY_URL, customerAPI.handleCustomerDelete, new JsonTransformer());
    	
    	exception(AppException.class, (exception, request, response) -> {
    	    response.status(500);
    	    logger.error("Application Exception!", exception);
    	    return;
    	});
    	
    	exception(AppInputException.class, (exception, request, response) -> {
    	    response.status(400);
    	    response.body(exception.getMessage());
    	    logger.error("Input Exception with message: {}", exception.getMessage(), exception);
    	    return;
    	});
    	
    	logger.info("Application Ready!");
    }
    
}

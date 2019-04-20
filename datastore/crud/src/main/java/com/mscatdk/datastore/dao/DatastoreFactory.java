package com.mscatdk.datastore.dao;

import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.common.collect.Lists;
import com.mscatdk.datastore.AppConfig;

public class DatastoreFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(DatastoreFactory.class);
	
	private DatastoreFactory() { }
	
	public static Datastore getInstance(AppConfig appConfig) throws IOException {
		Credentials credentials = getDatastoreCredentials(appConfig);
		
		String projectId = appConfig.projectId();
		String namespace = appConfig.namespace();
		
		DatastoreOptions options = DatastoreOptions.newBuilder()
				.setNamespace(namespace)
				.setProjectId(projectId)
				.setCredentials(credentials)
				.build();
				
		logger.info("Project ID: {}", projectId);
		logger.info("Namespace: {}", namespace);

		return options.toBuilder().build().getService();
	}
	
	public static CustomerDAO getCustomerDAO(Datastore datastore) {
		return new CustomerObjectifyDAO(datastore);
	}

    private static GoogleCredentials getDatastoreCredentials(AppConfig appConfig) throws IOException {
    	String credentialPath = appConfig.credentialPath();
    	
    	logger.info("Credential Path: {}", credentialPath);
    	return GoogleCredentials.fromStream(new FileInputStream(credentialPath))
		        .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
    }
}

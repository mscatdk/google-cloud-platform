package com.mscatdk.datastore.dao;

import java.io.FileInputStream;
import java.io.IOException;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.common.collect.Lists;
import com.mscatdk.datastore.AppConfig;

public class DatastoreFactory {
	
	private DatastoreFactory() { }
	
	public static Datastore getInstance(AppConfig appConfig) throws IOException {
		Credentials credentials = getDatastoreCredentials(appConfig);
		
		DatastoreOptions options = DatastoreOptions.newBuilder()
				.setNamespace(appConfig.namespace())
				.setProjectId(appConfig.projectId())
				.setCredentials(credentials)
				.build();

		return options.toBuilder().build().getService();
	}

    private static GoogleCredentials getDatastoreCredentials(AppConfig appConfig) throws IOException {
    	return GoogleCredentials.fromStream(new FileInputStream(appConfig.credentialPath()))
		        .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
    }
}

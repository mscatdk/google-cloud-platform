package com.mscatdk.datastore;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources("classpath:AppConfig.properties")
public interface AppConfig extends Config {
	
	@Key("DATASTORE_PROJECT_ID")
	String projectId();
	
	@Key("GOOGLE_APPLICATION_CREDENTIALS")
	String credentialPath();
	
	@Key("DATASTORE_NAMESPACE")
	String namespace();

}

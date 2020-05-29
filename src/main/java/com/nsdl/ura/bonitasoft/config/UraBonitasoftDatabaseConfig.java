package com.nsdl.ura.bonitasoft.config;

import org.bonitasoft.engine.api.APIClient;
import org.springframework.beans.factory.annotation.Value;

public class UraBonitasoftDatabaseConfig {

	@Value("${org.bonitasoft.engine.database.bonita.db-vendor}")
	String dbVendor;

	@Value("${org.bonitasoft.engine.database.bonita.url}")
	String dbUrl;

	@Value("${org.bonitasoft.engine.database.bonita.user}")
	String dbUser;

	@Value("${org.bonitasoft.engine.database.bonita.password}")
	String dbpassword;

	//	//create the engine
	//BonitaEngine bonitaEngine = new BonitaEngine();
	//	engine.setBonitaDatabaseConfiguration(BonitaDatabaseConfiguration.builder()
	//            .dbVendor(dbVendor)
	//            .url(dbUrl)
	//            .user(dbUser)
	//            .password(dbpassword)
	//            .build());
	//	
	//	//start the engine
	//	engine.start();

	//create a client to use it
	APIClient client = new APIClient();
}

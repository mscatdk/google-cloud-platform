package com.mscatdk.bigtable;

import org.apache.hadoop.hbase.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import com.mscatdk.bigtable.command.CleanCommand;
import com.mscatdk.bigtable.command.Command;
import com.mscatdk.bigtable.command.InitCommand;
import com.mscatdk.bigtable.command.ReadCommand;
import com.mscatdk.bigtable.command.SimCommand;
import com.mscatdk.bigtable.command.WriteCommand;
import com.mscatdk.bigtable.das.BigTableDAO;

public class App  {
	
	@Parameter(names = { "-p", "--projectid"}, description = "Google Cloud Platform Project ID")
	private String projectId;
	
	@Parameter(names = { "-i", "--instanceid"}, description = "BigTable instance ID")
	private String instanceId;
	
	public static final String TABLE_NAME = "temperatur";
	public static final String COLUMN_FAMILY_NAME = "core";
	public static final String ROOM1_COLUMN_NAME = "room1";
	
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	private static final Logger console = LoggerFactory.getLogger("console");
	
	private static BigTableDAO bigTableDAO = new BigTableDAO();
	
    public static void main( String[] args ) {
    	App app = new App();
    	
    	JCommander jc = JCommander.newBuilder()
    			.addObject(app)
    		    .addCommand("init", new InitCommand(bigTableDAO))
    		    .addCommand("clean", new CleanCommand(bigTableDAO))
    		    .addCommand("write", new WriteCommand(bigTableDAO))
    		    .addCommand("read", new ReadCommand(bigTableDAO))
    		    .addCommand("sim", new SimCommand(bigTableDAO))
    		    .build();
    	try {
        	jc.setProgramName("GCP BigTable demo");
    		jc.parse(args);
    		
        	console.info("Project id: {}", app.getProjectId());
        	console.info("Instance id: {}", app.getInstanceId());
    		Command command = getCommand(jc);
    		
    		try (Connection connection = BigtableConfiguration.connect(app.getProjectId(), app.getInstanceId())) {
    			command.exec(connection);
    		}
    		console.info("Done!");
    	} catch (ParameterException e) {
    		jc.usage();
    		logger.error("Unable to parse arguments!", e);
    		System.exit(-1);
    	} catch (Exception ex) {
    		logger.error("Application error!", ex);
    		System.exit(-1);
    	}
    }
    
    private static Command getCommand(JCommander jc) {
    	JCommander cmd = jc.getCommands().get(jc.getParsedCommand());
    	if (cmd == null || cmd.getObjects().size() != 1) {
    		throw new IllegalStateException("Unable to parse command!");
    	}
    	
    	Object obj = cmd.getObjects().get(0);
    	if (!(obj instanceof Command)) {
    		throw new IllegalStateException("cmd.getObjects().get(0) object doesn't implement the Command interface!");
    	}
    	return (Command) obj;
    }

	public String getProjectId() {
		return projectId;
	}

	public String getInstanceId() {
		return instanceId;
	}
    
}

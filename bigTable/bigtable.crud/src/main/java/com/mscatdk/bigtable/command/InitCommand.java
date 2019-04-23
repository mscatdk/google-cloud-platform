package com.mscatdk.bigtable.command;

import java.io.IOException;

import org.apache.hadoop.hbase.client.Connection;

import com.beust.jcommander.Parameters;
import com.mscatdk.bigtable.App;
import com.mscatdk.bigtable.das.BigTableDAO;

@Parameters(commandDescription = "Create required tables and column famalies!")
public class InitCommand implements Command {
	
	private BigTableDAO bigTableDAO = new BigTableDAO();
	
	public InitCommand(BigTableDAO bigTableDAO) {
		this.bigTableDAO = bigTableDAO;
	}
	
	public void exec(Connection connection) throws IOException {
		bigTableDAO.createTable(connection, App.TABLE_NAME, App.COLUMN_FAMILY_NAME);
	}

}

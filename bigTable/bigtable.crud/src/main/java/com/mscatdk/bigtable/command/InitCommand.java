package com.mscatdk.bigtable.command;

import java.io.IOException;

import org.apache.hadoop.hbase.client.Connection;

import com.beust.jcommander.Parameters;
import com.mscatdk.bigtable.App;
import com.mscatdk.bigtable.das.BigTableDAO;

@Parameters(commandDescription = "Create required tables and column famalies!")
public class InitCommand implements Command {
	
	public void exec(Connection connection) throws IOException {
		BigTableDAO.getInstance().createTable(connection, App.TABLE_NAME, App.COLUMN_FAMILY_NAME);
	}

}

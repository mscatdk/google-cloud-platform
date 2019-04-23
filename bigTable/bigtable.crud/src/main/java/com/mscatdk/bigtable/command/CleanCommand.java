package com.mscatdk.bigtable.command;

import java.io.IOException;

import org.apache.hadoop.hbase.client.Connection;

import com.beust.jcommander.Parameters;
import com.mscatdk.bigtable.App;
import com.mscatdk.bigtable.das.BigTableDAO;

@Parameters(commandDescription = "Clean-up tables!")
public class CleanCommand implements Command {
	
	private BigTableDAO bigTableDAO = new BigTableDAO();
	
	public CleanCommand(BigTableDAO bigTableDAO) {
		this.bigTableDAO = bigTableDAO;
	}

	public void exec(Connection connection) throws IOException {
		bigTableDAO.deleteTable(connection, App.TABLE_NAME);
	}

}

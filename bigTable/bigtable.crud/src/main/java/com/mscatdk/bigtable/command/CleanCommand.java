package com.mscatdk.bigtable.command;

import java.io.IOException;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;

import com.beust.jcommander.Parameters;
import com.mscatdk.bigtable.App;

@Parameters(commandDescription = "Clean-up tables!")
public class CleanCommand implements Command {

	public void exec(Connection connection) throws IOException {
		Admin admin = connection.getAdmin();
		
		if (!admin.tableExists(TableName.valueOf(App.TABLE_NAME))) {
			throw new IllegalStateException("Table doesn't exist!");
		}
		
		admin.disableTable(TableName.valueOf(App.TABLE_NAME));
		admin.deleteTable(TableName.valueOf(App.TABLE_NAME));
	}

}

package com.mscatdk.bigtable.command;

import org.apache.hadoop.hbase.client.Connection;

public interface Command {
	
	void exec(Connection connection) throws Exception;

}

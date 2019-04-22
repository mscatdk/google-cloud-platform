package com.mscatdk.bigtable.command;

import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.util.Bytes;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.mscatdk.bigtable.App;
import com.mscatdk.bigtable.das.BigTableDAO;

@Parameters(commandDescription = "Write sensor value to BigTable.")
public class WriteCommand implements Command {
	
	@Parameter(names = { "-s", "--sensorid"}, description = "Sensor ID")
	private String sensorId;
	
	@Parameter(names = { "-v", "--value"}, description = "Temperatur Measurement")
	private Long value;

	public void exec(Connection connection) throws IOException {
		String rowkey = sensorId + (Long.MAX_VALUE - new Date().getTime());
		BigTableDAO.getInstance().write(connection, App.TABLE_NAME, App.COLUMN_FAMILY_NAME, App.ROOM1_COLUMN_NAME, rowkey, Bytes.toBytes(value));
	}

}

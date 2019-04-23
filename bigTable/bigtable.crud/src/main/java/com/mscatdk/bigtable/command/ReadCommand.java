package com.mscatdk.bigtable.command;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.mscatdk.bigtable.App;
import com.mscatdk.bigtable.das.BigTableDAO;

@Parameters(commandDescription = "Read sensor value from BigTable!")
public class ReadCommand implements Command {
	
	@Parameter(names = { "-s", "--sensorid"}, description = "Sensor ID")
	private String sensorId;
	
	private static final Logger console = LoggerFactory.getLogger("console");
	
	private BigTableDAO bigTableDAO = new BigTableDAO();
	
	public ReadCommand(BigTableDAO bigTableDAO) {
		this.bigTableDAO = bigTableDAO;
	}

	public void exec(Connection connection) throws IOException {
		Map<String, byte[]> data = bigTableDAO.readColumn(connection, App.TABLE_NAME, App.COLUMN_FAMILY_NAME, App.ROOM1_COLUMN_NAME, sensorId);
		
		for (Map.Entry<String, byte[]> entity : data.entrySet()) {
			console.info("KeyID: {} Value: {}", entity.getKey(), Bytes.toLong(entity.getValue()));
		}
	}

}

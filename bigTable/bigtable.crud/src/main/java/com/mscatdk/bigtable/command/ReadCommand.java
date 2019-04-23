package com.mscatdk.bigtable.command;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
	
	@Parameter(names = "--from", description = "From timestamp")
	private String from;

	@Parameter(names = "--to", description = "From timestamp")
	private String to;
	
	private static final Logger console = LoggerFactory.getLogger("console");
	
	DateTimeFormatter dtf = DateTimeFormat.forPattern(App.TIMESTAMP_FORMAT);
	private BigTableDAO bigTableDAO = new BigTableDAO();
	
	public ReadCommand(BigTableDAO bigTableDAO) {
		this.bigTableDAO = bigTableDAO;
	}

	public void exec(Connection connection) throws IOException, ParseException {
		Map<String, byte[]> data;
		if (from == null && to==null) {
			data = bigTableDAO.readColumnByPreFix(connection, App.TABLE_NAME, App.COLUMN_FAMILY_NAME, App.ROOM1_COLUMN_NAME, sensorId);
		} else {
			data = bigTableDAO.readColumnByTimeRange(connection, App.TABLE_NAME, App.COLUMN_FAMILY_NAME, App.ROOM1_COLUMN_NAME, sensorId, from, to);
		}
			
		for (Map.Entry<String, byte[]> entity : data.entrySet()) {
			String keyTime = getTimestamp(entity.getKey());
			console.info("KeyID: {} Ket Time: {} Value: {}", entity.getKey(), keyTime, Bytes.toLong(entity.getValue()));
		}
	}

	public String getTimestamp(String key) {
		Long time = Long.parseLong(key.replaceAll(sensorId, ""));
		DateTime dateTime = new DateTime(Long.MAX_VALUE-time);
		return dateTime.toString(dtf);
	}
}

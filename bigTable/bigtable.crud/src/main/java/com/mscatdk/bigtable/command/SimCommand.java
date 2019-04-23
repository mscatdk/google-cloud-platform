package com.mscatdk.bigtable.command;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.mscatdk.bigtable.App;
import com.mscatdk.bigtable.das.BigTableDAO;

public class SimCommand implements Command {
	
	@Parameter(names = { "-s", "--sensorid"}, description = "Sensor ID")
	private String sensorId;
	
	private static final Logger console = LoggerFactory.getLogger("console");
	
	private BigTableDAO bigTableDAO = new BigTableDAO();
	
	public SimCommand(BigTableDAO bigTableDAO) {
		this.bigTableDAO = bigTableDAO;
	}

	public void exec(Connection connection) throws IOException {
		Long temp = 25L;
		
		while(true) {
			String rowkey = sensorId + (Long.MAX_VALUE - new Date().getTime());
			bigTableDAO.write(connection, App.TABLE_NAME, App.COLUMN_FAMILY_NAME, App.ROOM1_COLUMN_NAME, rowkey, Bytes.toBytes(temp));
			console.info("Wrote entry for sensoe: {} with value: {}", sensorId, temp);
			temp = temp + getRandomNumberInRange(-5, 5);
			
			wait(200);
		}
	}
	
	private void wait(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			
		}
	}

	private int getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
}

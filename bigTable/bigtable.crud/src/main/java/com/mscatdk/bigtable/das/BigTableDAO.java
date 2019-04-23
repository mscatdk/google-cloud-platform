package com.mscatdk.bigtable.das;

import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.MultiRowRangeFilter;
import org.apache.hadoop.hbase.filter.MultiRowRangeFilter.RowRange;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.shaded.com.google.common.collect.Lists;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.mscatdk.bigtable.App;

public class BigTableDAO {
	
	private DateTimeFormatter dtf = DateTimeFormat.forPattern(App.TIMESTAMP_FORMAT);
	
	public void deleteTable(Connection connection, String tableName) throws IOException {
		Admin admin = connection.getAdmin();
		
		if (admin.tableExists(TableName.valueOf(tableName))) {
			admin.disableTable(TableName.valueOf(tableName));
			admin.deleteTable(TableName.valueOf(tableName));
		}
	}
	
	public void createTable(Connection connection, String tableName, String... columnFamilyNames) throws IOException {
		Admin admin = connection.getAdmin();
		
		if (admin.tableExists(TableName.valueOf(tableName))) {
			throw new IllegalStateException("Table has already been created!");
		}
		
		HTableDescriptor descriptor = new HTableDescriptor(TableName.valueOf(tableName));
		for (String columnFamily : columnFamilyNames) {
			descriptor.addFamily(new HColumnDescriptor(columnFamily));
		}
		
		admin.createTable(descriptor);
	}
	
	public Map<String, byte[]> readColumnByPreFix(Connection connection, String tableName, String columnFamilyName, String column, String keyPrefix) throws IOException {
		return readColumn(connection, tableName, columnFamilyName, column, new PrefixFilter(Bytes.toBytes(keyPrefix)));
	}
	
	public Map<String, byte[]> readColumnByTimeRange(Connection connection, String tableName, String columnFamilyName, String column, String sensorId, String fromTime, String toTime) throws IOException, ParseException {
		Long from = DateTime.parse(fromTime, dtf).getMillis();
		Long to = toTime == null ? DateTime.now().getMillis() : DateTime.parse(toTime, dtf).getMillis();
		
		List<RowRange> ranges = Lists.newArrayList();
		ranges.add(new RowRange(getRowId(sensorId, to), true, getRowId(sensorId, from), true));
		
		return readColumn(connection, tableName, columnFamilyName, column, new MultiRowRangeFilter(ranges));
	}
	
	private byte[] getRowId(String sensorId, Long time) {
		String key = sensorId + (Long.MAX_VALUE - time);
		return Bytes.toBytes(key);
	}
	
	public Map<String, byte[]> readColumn(Connection connection, String tableName, String columnFamilyName, String column, Filter filter) throws IOException {
		Table table = connection.getTable(TableName.valueOf(tableName));
		
		Scan scan = new Scan();
		scan.setFilter(filter);
		ResultScanner scanner = table.getScanner(scan);
		
		int i = 0;
		Map<String, byte[]> data = new LinkedHashMap<>();
		for (Result result : scanner) {
			String key = Bytes.toString(result.getRow());
			byte[] value = result.getValue(Bytes.toBytes(columnFamilyName), Bytes.toBytes(column));
			
			data.put(key, value);
			i++;
			if (i>100) break;
		}
		
		return data;
	}
	
	public void write(Connection connection, String tableName, String columnFamilyName, String column, String rowkey, byte[] value) throws IOException {
		 Table table = connection.getTable(TableName.valueOf(tableName));
		 
		 Put put = new Put(Bytes.toBytes(rowkey));
		 put.addColumn(Bytes.toBytes(columnFamilyName), Bytes.toBytes(column), value);
		 table.put(put);
	}

}

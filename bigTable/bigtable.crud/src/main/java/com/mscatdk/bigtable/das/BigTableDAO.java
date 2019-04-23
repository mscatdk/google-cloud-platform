package com.mscatdk.bigtable.das;

import java.io.IOException;
import java.util.HashMap;
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
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;

public class BigTableDAO {
	
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
	
	public Map<String, byte[]> readColumn(Connection connection, String tableName, String columnFamilyName, String column, String keyPrefix) throws IOException {
		Table table = connection.getTable(TableName.valueOf(tableName));
		
		Filter filter = new PrefixFilter(Bytes.toBytes(keyPrefix));
		
		Scan scan = new Scan();
		scan.setFilter(filter);
		ResultScanner scanner = table.getScanner(scan);
		
		int i = 0;
		Map<String, byte[]> data = new HashMap<>();
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

package com.mscatdk.dataflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * 
 */
public class StarterPipeline {
  private static final Logger LOG = LoggerFactory.getLogger(StarterPipeline.class);

	public static interface MyOptions extends DataflowPipelineOptions {
		@Description("PubSub topic path")
		String getPubSubTopicPath();

		void setPubSubTopicPath(String s);
		
		@Description("BigQuery Table used for output")
		String getBigQueryTable();

		void setBigQueryTable(String s);
	}
	
	private static final String ROOM_COLUMN_NAME = "room";
	private static final String TEMPERATUR_COLUMN_NAME = "temperatur";
  
  @SuppressWarnings("serial")
  public static void main(String[] args) {
	PipelineOptionsFactory.register(MyOptions.class);
	MyOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(MyOptions.class);
	options.setStreaming(true);
	
	Pipeline p = Pipeline.create(options);
    
	String pubSubTopic = options.getPubSubTopicPath();
    String bigQueryTable = options.getBigQueryTable();
    
    // Define schema
    List<TableFieldSchema> fields = new ArrayList<>();
    fields.add(new TableFieldSchema().setName(ROOM_COLUMN_NAME).setType("STRING"));
    fields.add(new TableFieldSchema().setName(TEMPERATUR_COLUMN_NAME).setType("INTEGER"));
    TableSchema schema = new TableSchema().setFields(fields);
    
    p	.apply("Read from pubsub", PubsubIO.readMessagesWithAttributes().fromTopic(pubSubTopic).withTimestampAttribute("ts"))
    	.apply("Process message", ParDo.of(new DoFn<PubsubMessage, TableRow>() {
			@ProcessElement
			public void processElement(ProcessContext c) {
				PubsubMessage message = c.element();
				String data = new String(message.getPayload());
				JsonObject jsonObject = new Gson().fromJson(data, JsonObject.class);
				LOG.debug("JSON string: {}", data);
				
				if ("DK".equals(jsonObject.get("country").getAsString())) {
					String room = jsonObject.get("room").getAsString();
					Integer temp  = jsonObject.get("temperatur").getAsInt();
					TableRow row = new TableRow()
						.set(ROOM_COLUMN_NAME, room)
						.set(TEMPERATUR_COLUMN_NAME, temp);
					
					c.output(row);
				}
			}    		
		}))
    	.apply("Insert data into BigQuery", BigQueryIO.writeTableRows()
    											.to(bigQueryTable)
    											.withSchema(schema)
    											.withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
    											.withWriteDisposition(WriteDisposition.WRITE_APPEND));

    p.run().waitUntilFinish();
  }
}
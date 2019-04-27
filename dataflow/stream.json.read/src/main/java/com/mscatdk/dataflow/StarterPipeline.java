package com.mscatdk.dataflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Mean;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
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
	private static final String TEMPERATUR_COLUMN_NAME = "mean_temperature";
	private static final DateTimeFormatter dtf = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");
  
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
    fields.add(new TableFieldSchema().setName(TEMPERATUR_COLUMN_NAME).setType("Float"));
    fields.add(new TableFieldSchema().setName(TEMPERATUR_COLUMN_NAME).setType("Datetime"));
    TableSchema schema = new TableSchema().setFields(fields);
    
    p	.apply("Read from pubsub", PubsubIO.readMessagesWithAttributes().fromTopic(pubSubTopic).withTimestampAttribute("ts"))
    	.apply("Process message", ParDo.of(new DoFn<PubsubMessage, KV<String, Integer>>() {
			@ProcessElement
			public void processElement(ProcessContext c) {
				PubsubMessage message = c.element();
				String data = new String(message.getPayload());
				JsonObject jsonObject = new Gson().fromJson(data, JsonObject.class);
				LOG.debug("JSON string: {}", data);
				
				if ("DK".equals(jsonObject.get("country").getAsString())) {
					String room = jsonObject.get("room").getAsString();
					Integer temp  = jsonObject.get("temperature").getAsInt();
					
					c.output(KV.of(room, temp));
				}
			}    		
		}))
    	.apply("Fixed window", Window.<KV<String, Integer>>into(FixedWindows.of(Duration.standardSeconds(10)))
    																.triggering(
    																	AfterWatermark.pastEndOfWindow()
    																		.withEarlyFirings(AfterProcessingTime.pastFirstElementInPane().plusDelayOf(Duration.standardSeconds(10)))
    																		.withLateFirings(AfterProcessingTime.pastFirstElementInPane().plusDelayOf(Duration.standardSeconds(30)))
    																)
    																.withAllowedLateness(Duration.standardSeconds(3))
    																.accumulatingFiredPanes())
    	.apply("Calculate Mean", Mean.perKey())
    	.apply("Map to table", ParDo.of(new DoFn<KV<String, Double>, TableRow>() {
    		@Setup
    		public void setup() {
    			LOG.debug("Calculate mean @ {}", getFormatedTime());
    		}
    		
    		@ProcessElement
    		public void processElement(ProcessContext c) {
    			KV<String, Double> value = c.element();
				TableRow row = new TableRow()
						.set(ROOM_COLUMN_NAME, value.getKey())
						.set(TEMPERATUR_COLUMN_NAME, value.getValue());			
					
					c.output(row);
    		}
    		
    	}))
    	.apply("Insert data into BigQuery", BigQueryIO.writeTableRows()
    											.to(bigQueryTable)
    											.withSchema(schema)
    											.withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
    											.withWriteDisposition(WriteDisposition.WRITE_APPEND));

    p.run().waitUntilFinish();
  }
  
	private static String getFormatedTime() {
		return DateTime.now().toString(dtf);
	}

}
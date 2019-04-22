package com.mscatdk.dataflow;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.values.KV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * 
 */
public class StarterPipeline {
  private static final Logger LOG = LoggerFactory.getLogger(StarterPipeline.class);

	public static interface MyOptions extends PipelineOptions {
		@Description("Output prefix")
		@Default.String("/tmp/output")
		String getOutputPrefix();

		void setOutputPrefix(String s);
		
		@Description("Input directory")
		@Default.String("/tmp/data/club.list.txt")
		String getInput();

		void setInput(String s);
	}
  
  @SuppressWarnings("serial")
  public static void main(String[] args) {
	MyOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(MyOptions.class);
	Pipeline p = Pipeline.create(options);
    
    String input = options.getInput();
    String outputPrefix = options.getOutputPrefix();
    
    p	.apply("Read Json", TextIO.read().from(input))
    	.apply("Create Key value pair", ParDo.of(new DoFn<String,KV<String, Integer>>() {
			@ProcessElement
			public void processElement(ProcessContext c) {
				String v = c.element();
				
				LOG.debug("JSON string: {}", v);
				JsonObject jsonObject = new Gson().fromJson(v, JsonObject.class);
				
				if ("Master".equals(jsonObject.get("Type").getAsString())) {
					String club = jsonObject.get("Club").getAsString();
					Integer members = jsonObject.get("Members").getAsInt();
					c.output(KV.of(club, members));
				}
			}    		
		}))
    	.apply(Sum.integersPerKey())
    	.apply("To String", ParDo.of(new DoFn<KV<String, Integer>, String>() {
    		@ProcessElement
    		public void processElement(ProcessContext c) {
    			KV<String, Integer> data = c.element();
    			
    			c.output(String.format("%s: %d", data.getKey(), data.getValue()));
    		}
    	}))
    	.apply("Create output", TextIO.write().to(outputPrefix).withSuffix(".csv").withoutSharding());

    p.run();
  }
}

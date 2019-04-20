package com.mscatdk.datastore;

import com.google.gson.Gson;

import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {

    private Gson gson = new Gson();

    @Override
    public String render(Object model) {
    	if (model == null) {
    		return "";
    	} else {
    		return gson.toJson(model);
    	}
    }

}

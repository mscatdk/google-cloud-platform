package com.mscatdk.datastore.dao;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static spark.Spark.after;
import static spark.Spark.before;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.datastore.Datastore;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import com.mscatdk.datastore.model.Customer;

import spark.Request;
import spark.Response;

public class CustomerObjectifyDAO implements CustomerDAO {
	
	private static final Logger logger = LoggerFactory.getLogger(CustomerObjectifyDAO.class);
	
	private static final String OBJECTIFY_SERVICE_ATTRIBUTE_NAME="OBJECTIFY_SERVICE_ATTRIBUTE";
	
	public CustomerObjectifyDAO(Datastore datastore) {
		/*
		 * Below code is required by Objectify; however, it would be better to add it to a context listner and the actual filter chain. 
		 * It has been added below as SparkJava doesn't allow easy access to the filter chain and to capture all changes needed for the 
		 * Objectify implementation in this class.
		 */
		ObjectifyService.init(new ObjectifyFactory(datastore));
        ObjectifyService.register(Customer.class);
        
    	before((Request request, Response response) -> {
    		Closeable closeable = ObjectifyService.begin();
    		request.attribute(OBJECTIFY_SERVICE_ATTRIBUTE_NAME, closeable);
    	});
    	
    	after((Request request, Response response) -> {
    		Closeable closeable = request.attribute(OBJECTIFY_SERVICE_ATTRIBUTE_NAME);
    		closeable.close();
    		request.attribute(OBJECTIFY_SERVICE_ATTRIBUTE_NAME, null);
    	});
	}
	
	public List<Customer> list() {
		return ofy().load().type(Customer.class).limit(10).list();
	}
	
	public void update(Long id, Customer customer) {
		customer.setId(id);
		logger.debug("Update customer with id: {}", id);
		ofy().save().entities(customer).now();
	}

	public Customer get(Long id) {
		logger.debug("Get customer with id: {}", id);
		return ofy().load().type(Customer.class).id(id).now();
	}
	
	public Customer put(Customer customer) {
		ofy().save().entity(customer).now();
		logger.debug("Create customer with id: {}", customer.getId());
		return customer;
	}
	
	public void delete(Long id) {
		logger.debug("Delete customer with id: {}", id);
		ofy().delete().key(Key.create(Customer.class, id)).now();
	}
	
}

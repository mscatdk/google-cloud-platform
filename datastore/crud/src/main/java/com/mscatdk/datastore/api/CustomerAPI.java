package com.mscatdk.datastore.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.HttpStatusCodes;
import com.google.gson.Gson;
import com.mscatdk.datastore.AppInputException;
import com.mscatdk.datastore.dao.Customer;
import com.mscatdk.datastore.dao.CustomerDAO;

import spark.Request;
import spark.Response;
import spark.Route;

public class CustomerAPI {
	
	private static final Logger logger = LoggerFactory.getLogger(CustomerAPI.class);
	
	private final CustomerDAO customerDAO;
	private final Gson gson = new Gson();
	public static final String ID_PARAM_NAME = ":customerId";
	
	public CustomerAPI(CustomerDAO customerDAO) {
		super();
		this.customerDAO = customerDAO;
	}
	
	private CustomerDAO getCustomerDAO() {
		return this.customerDAO;
	}
	
	public final Route handleCustomerList = (Request request, Response response) -> {
		return getCustomerDAO().list();
	};

	public final Route handleCustomerGet = (Request request, Response response) -> {
		Long id = getId(request);
		Customer customer = getCustomerDAO().get(id);
		
		if (customer == null) {
			response.status(HttpStatusCodes.STATUS_CODE_NO_CONTENT);
			logger.debug("Unable to find customer with id: {}", id);
		} 
		
		return customer;
	};
	
	public final Route handleCustomerCreate = (Request request, Response response) -> {
		Customer customer = getCustomer(request);
		customer = getCustomerDAO().put(customer);
		response.status(HttpStatusCodes.STATUS_CODE_CREATED);
		
		return customer;
	};
	
	public final Route handleCustomerDelete = (Request request, Response response) -> {
		getCustomerDAO().delete(getId(request));
		response.status(HttpStatusCodes.STATUS_CODE_OK);
		return null;
	};
	
	public final Route handleCustomerUpdate = (Request request, Response response) -> {
		getCustomerDAO().update(getId(request), getCustomer(request));
		response.status(HttpStatusCodes.STATUS_CODE_OK);
		return null;
	};
	
	private Long getId(Request request) {
		try {
			String param = request.params(ID_PARAM_NAME);
			return Long.parseLong(param);
		} catch (Exception e) {
			throw new AppInputException("Unable to parse ID", e);
		}
	}

	private Customer getCustomer(Request request) {
		return gson.fromJson(request.body(), Customer.class);
	}
}
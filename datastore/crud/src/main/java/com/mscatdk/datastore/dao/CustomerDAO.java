package com.mscatdk.datastore.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;

public class CustomerDAO {
	
	private Datastore datastore;
	private KeyFactory keyFactory;
	
	private static final Logger logger = LoggerFactory.getLogger(CustomerDAO.class);
	
	public CustomerDAO(Datastore datastore) {
		this.datastore = datastore;
		this.keyFactory = datastore.newKeyFactory().setKind(Customer.ENTITY_KIND);
	}
	
	public List<Customer> list() {
		Query<Entity> query = Query.newEntityQueryBuilder()
		            .setKind(Customer.ENTITY_KIND)
		            .setLimit(20)
		            .build();
		 
		Iterator<Entity> entities = datastore.run(query);
		
		logger.debug("List customers");
		return buildCustomer(entities);
	}
	
	public void update(Long id, Customer customer) {
		Key key = keyFactory.newKey(id);
	
		Entity entity = Entity.newBuilder(key)
				.set(Customer.FIRST_NAME,  customer.getFirstName())
				.set(Customer.LAST_NAME, customer.getLastName())
				.build();
		
		logger.debug("Update customer with id: {}", id);
		datastore.put(entity);
	}

	public Customer get(Long id) {
		Key key = keyFactory.newKey(id);
		Entity entity = datastore.get(key);
		
		logger.debug("Get customer with id: {}", id);
		return entityToCustomer(entity);
	}
	
	public Customer put(Customer customer) {
		IncompleteKey key = keyFactory.newKey();
		FullEntity<IncompleteKey> entity = Entity.newBuilder(key)
								.set(Customer.FIRST_NAME,  customer.getFirstName())
								.set(Customer.LAST_NAME, customer.getLastName())
								.build();
		
		Entity obj = datastore.put(entity);
		
		logger.debug("Create customer with id: {}", obj.getKey().getId());
		return entityToCustomer(obj);
	}
	
	public void delete(Long id) {
		Key key = keyFactory.newKey(id);
		
		logger.debug("Delete customer with id: {}", id);
		datastore.delete(key);
	}
	
    private List<Customer> buildCustomer(Iterator<Entity> entities){
        List<Customer> questions = new ArrayList<>();
        entities.forEachRemaining(entity-> questions.add(entityToCustomer(entity)));
        return questions;
    }

    private Customer entityToCustomer(Entity entity){
    	if (entity == null) {
    		return null;
    	}
    	
        return new Customer(entity.getKey().getId(),
        					entity.getString(Customer.FIRST_NAME), 
        					entity.getString(Customer.LAST_NAME));
    }
}

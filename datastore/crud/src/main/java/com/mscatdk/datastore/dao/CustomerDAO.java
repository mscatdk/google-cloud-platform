package com.mscatdk.datastore.dao;

import java.util.List;

import com.mscatdk.datastore.model.Customer;

public interface CustomerDAO {
	
	public List<Customer> list();
	
	public void update(Long id, Customer customer);

	public Customer get(Long id);
	
	public Customer put(Customer customer);
	
	public void delete(Long id);

}

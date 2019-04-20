package com.mscatdk.datastore;

public class AppInputException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1891944953343847210L;

	public AppInputException(String message, Exception e) {
		super(message, e);
	}
	
	public AppInputException(String message) {
		super(message);
	}
}

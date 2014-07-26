package com.crec.controller.event.validate; 

import java.util.ArrayList;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

public class Validate { 
	
	private static Logger log = Logger.getLogger(Validate.class); 
	private String next; 
	private ArrayList<Operation> operations = new ArrayList<Operation>();
	
	public Validate(){}
	
	public Validate(String next) {
		super(); 
		this.next = next == null ? "END" : next; 
	} 
	
	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	} 

	public void addOperation(Operation operation){
		this.operations.add(operation);
	}
    
    public boolean validate(JSONObject result){ 
    	//todo 对组合条件的支持
    	log.debug("  start validate[" + result + "]"); 
    	for(Operation op : operations){
    		if(!op.compare(result)){
    			return false;
    		}
    	}
    	return true;
    } 

}

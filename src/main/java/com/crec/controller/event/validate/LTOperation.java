package com.crec.controller.event.validate;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
/**
 * 小于比较的
 * @author Administrator
 *
 */
public class LTOperation extends ValOperation{
	
	private static Logger log = Logger.getLogger(LTOperation.class);
	  
	public LTOperation(){}

	public LTOperation(String value, String paramString) {
		super(value, paramString); 
	}

	@Override
	public boolean compare(JSONObject resource) {
		// TODO Auto-generated method stub
		Object obj = this.getValueByParamString(resource);
		log.debug("operation[" + obj + "]<[" + this.getValue() + "]");  
		
		return String.valueOf(obj).compareTo(this.getValue()) < 0;
	}

}

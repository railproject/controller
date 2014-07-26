package com.crec.controller.event.validate;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
/**
 * 等于比较的实现
 * @author Administrator
 *
 */
public class EQOperation extends ValOperation{
	
	private static Logger log = Logger.getLogger(EQOperation.class);
	  
	public EQOperation(){}

	public EQOperation(String value, String paramString) {
		super(value, paramString); 
	}

	@Override
	public boolean compare(JSONObject resource) {
		// TODO Auto-generated method stub
		Object obj = this.getValueByParamString(resource);
		log.debug("operation[" + obj + "]==[" + this.getValue() + "]");
		
		return String.valueOf(obj).equals(this.getValue());
	}

}

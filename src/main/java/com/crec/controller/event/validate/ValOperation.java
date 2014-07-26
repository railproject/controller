package com.crec.controller.event.validate;

import net.sf.json.JSONObject;

import com.crec.controller.utile.JsonValueHelper;
/*
 * 值分支的超类
 */
public abstract class ValOperation  extends Operation{
	
	private String value;
	
	private String paramName; 
	
	public ValOperation(){} 
	
	public ValOperation(String value, String paramName) { 
		super();
		this.value = value;
		this.paramName = paramName;
	} 

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	} 
	
	protected Object getValueByParamString(JSONObject resource){
		return JsonValueHelper.getValueByParamString(resource, this.getParamName().split("\\."));
	}
	
}

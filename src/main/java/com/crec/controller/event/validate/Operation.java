package com.crec.controller.event.validate;
import net.sf.json.JSONObject;

public abstract class Operation { 
	 
	protected abstract boolean compare(JSONObject resource);
	
	
	public static void main(String[] args) { 
		 
		System.out.println("123".compareTo("adfa"));
		System.out.println("------------------------------------");
		JSONObject obj = JSONObject.fromObject("{a:[{\"b\":2},{\"b\":3}]}".getBytes());
		System.out.println(obj.getJSONArray("a").getJSONObject(0).get("b")); 
	}
 
}

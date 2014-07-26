package com.crec.controller.event.validate;

import net.sf.json.JSONObject;
/**
 * AND的逻辑实现
 * @author Administrator
 *
 */
public class AndOperation extends SymbolOperation{ 
	 
	public boolean compare(JSONObject resource) {
		// TODO Auto-generated method stub 
		for(Operation operation :  operations){
			if(!operation.compare(resource)){
				return false;
			} 
		}
		return true;
	}
	
	public static void main(String[] args) {
		System.out.println(AndOperation.class.isAssignableFrom(Operation.class));
	}

}

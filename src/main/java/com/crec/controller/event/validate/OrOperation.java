package com.crec.controller.event.validate;

import net.sf.json.JSONObject;

/**
 * or的逻辑实现
 * @author Administrator
 *
 */
public class OrOperation extends SymbolOperation{ 
	
	public boolean compare(JSONObject resource) {
		// TODO Auto-generated method stub 
		for(Operation operation :  operations){
			if(operation.compare(resource)){
				return true;
			} 
		}
		return false;
	}
}

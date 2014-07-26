package com.crec.controller.utile;

import java.util.HashMap;
import java.util.Map;

public class ClassMap { 
	
	private static Map<String, Class<?>> classMap = new HashMap<String, Class<?>>(); 
	
	static{
		new ClassMap(Constants.EVENT_CLASS_KEY, com.crec.controller.event.action.Event.class);
		new ClassMap("eq" + Constants.OPERATION_CLASS_KEY, com.crec.controller.event.validate.EQOperation.class);
		new ClassMap("rest" + Constants.ACTION_CLASS_KEY, com.crec.controller.event.action.RestAction.class);
		new ClassMap(Constants.VALIDATE_CLASS_KEY, com.crec.controller.event.validate.Validate.class); 
		new ClassMap("lt" + Constants.OPERATION_CLASS_KEY, com.crec.controller.event.validate.LTOperation.class);
		new ClassMap("and" + Constants.OPERATION_CLASS_KEY, com.crec.controller.event.validate.AndOperation.class);
		new ClassMap("or" + Constants.OPERATION_CLASS_KEY, com.crec.controller.event.validate.OrOperation.class);
		
		new ClassMap("rabbitmq" + Constants.ACTION_CLASS_KEY, com.crec.controller.event.action.RabbitMqAction.class);
		 
		
		
	}
	
	 
	private ClassMap(String name, Class<?> c){
		classMap.put(name.toUpperCase(), c);
	}
	
	public static Class<?> getClass(String name, String type){
		String key = type == null ? name : type + "_" + name; 
		 System.out.println(key.toUpperCase());
		return classMap.get(key.toUpperCase());
		
	} 
	
	public static Class<?> getClass(String name){   
		return classMap.get(name.toUpperCase());
		
	}  
	
	public static boolean containsClass(String name, String type){
		String key = type == null ? name : name + "_" + type; 
		return classMap.containsKey(key);
	} 
	
	public static boolean containsClass(String name){ 
		return classMap.containsKey(name);
	}
}

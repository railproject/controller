package com.crec.controller.utile;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Test {  
	 
	public static void main(String[] args) throws InterruptedException, IOException {
//		org.springframework.amqp.rabbit.connection.SimpleConnectionFactory
//		ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");  
//		 SpringBeanUtils.setSpringContext(app);
		 
		 
		 String regex = "^(.+?)[\\(（](.+?)[\\)）]";
	     Pattern pattern = Pattern.compile(regex);
	     Matcher matcher = pattern.matcher("G1(北京）"); 
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) { 
            String valuekey = matcher.group(1);//键名 
            System.out.println(valuekey + "----" + matcher.group(2));
             
        }
//		Map<String, String> map = new HashedMap();
//		
//		map.put("code", "404");
//		map.put("1313", "123123");
//		
//		JSONObject json = new JSONObject();
//		json.put("code", "200");
//		json.putAll(map); 
//		
//		System.out.println(json.toString());
		
 //		app.destroy();
		
	   
		
//		String a = "你好";
//		System.out.println(a.getBytes());
	} 
	 
}
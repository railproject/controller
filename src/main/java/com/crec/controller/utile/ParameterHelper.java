package com.crec.controller.utile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

public class ParameterHelper {
	
	private static Logger log = Logger.getLogger(ParameterHelper.class); 
	
	public static String getReplacement(String str, JSONObject resource){  
		log.debug("-----------getReplacement------------" + str);
		log.debug("-----------resource------------" + resource);
		if(str != null && str.indexOf("${") > -1){ 
			 String regex = "\\$\\{(.+?)\\}";
		     Pattern pattern = Pattern.compile(regex);
		     Matcher matcher = pattern.matcher(str); 
	        StringBuffer sb = new StringBuffer();
	        while (matcher.find()) { 
	            String valuekey = matcher.group(1);//键名 
	            String value = String.valueOf(JsonValueHelper.getValueByParamString(resource, valuekey.split("\\.")));//键值
	            matcher.appendReplacement(sb, value);  
	        }
	        matcher.appendTail(sb);
	        log.debug("-----------replace------------" + sb);
	        return sb.toString();
		} 
		log.debug("-----------noreplace------------" + str);
		return str;
	} 
	
	public static void main(String[] args) {
		JSONObject obj = JSONObject.fromObject("{a:{'b':2, 'c':[[4,5],[5,6],[{'u':9}]]}}");
		
		System.out.println(getReplacement("{${a.b}, ${a.c[0][1]}, ${a.c[2][0].u}}", obj));
		
		System.out.println();
	}
}

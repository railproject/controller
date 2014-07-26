package com.crec.controller.utile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JsonValueHelper {
	private static Logger log = Logger.getLogger(ParameterHelper.class); 
	
	/**
	 * 重json对象中获取某属性的值
	 * @param json 需要取值的json对象
	 * @param paramString 有层级关系的属性的 数组 如 ["a", "b", "c[1]"] 表示  json.getJSONObject("a").getJSONObject("b").getJSONArray("c").get(1)
	 * @return
	 */
	public static Object getValueByParamString(JSONObject json, String[] paramString){ 
		 
		Object nextObj = null;
		 log.debug("replace JsonValueHelper.getValueByParamString params:");
		 log.debug(json);
		 log.debug(paramString);
		 if(paramString.length == 1){ 
			
			return  getCurrentNode(json, paramString[0]);
		}else{ 
			//todo 加入对JSONArray 的支持   
			nextObj = getCurrentNode(json, paramString[0]); 
			if(JSONObject.class.isAssignableFrom(nextObj.getClass())){ 
				String[] paths = new String[paramString.length - 1];
			    System.arraycopy(paramString, 1, paths, 0, paramString.length -1); 
			    nextObj = getValueByParamString((JSONObject)nextObj, paths); 
			} 
		} 
		return nextObj;
	} 
	
	private static Object getCurrentNode(JSONObject json, String nodeName){
		log.debug("current nodeName:" + nodeName);
		log.debug("current result josn" + json);
		int startIndex = nodeName.indexOf("[");  
		Object result = null;
		String currentParam = "";  
		if(startIndex > -1){
			log.debug("is a array object");
			 currentParam = nodeName.substring(0, startIndex);
			 String regex = "\\[(\\d+)\\]";
		     Pattern pattern = Pattern.compile(regex);
		     Matcher matcher = pattern.matcher(nodeName);  
		     log.debug("current key:" + currentParam); 
		     JSONArray jr = json.getJSONArray(currentParam);
		     boolean flag = true;
		     if(matcher.find()){
		        while (flag) { 
		            String valuekey = matcher.group(1);//元素位置 
		            if(!matcher.find()){//如果没有了就直接去结果出来 
		            	result = jr.get(Integer.parseInt(valuekey)); 
		            	flag = false;
		            }else{ //如果还有继续取 
		            	jr = jr.getJSONArray(Integer.parseInt(valuekey));
		            }
		        }
		     }else{
		    	 log.error("here get a array mapping, but not get the array index");
		    	 result = "";
		     }
		}else{
			log.debug("here is a common param:" + json);
			log.debug("here is a common param:" + nodeName);
			result = json.get(nodeName);
		} 
		return result;
	}
	
	public static void main(String[] args) {
		JSONObject obj = JSONObject.fromObject("{a:{\"b\":2, \"c\":[[4,5],[5,6],[{\"u\":9}]]}}");
		System.out.println(getValueByParamString(obj, "a.c[1]".split("\\.")));
		System.out.println(getValueByParamString(obj, "a.c[1][1]".split("\\.")));
		System.out.println(getValueByParamString(obj, "a.c[2][0]".split("\\.")));
		System.out.println(getValueByParamString(obj, "a.c[2][0].u".split("\\.")));
	}
}

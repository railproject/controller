package com.crec.controller.engine;

import java.util.HashMap;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import com.crec.controller.event.action.Event;
import com.crec.controller.event.action.Process;
import com.crec.controller.utile.Constants;
import com.crec.controller.utile.EventConfigHelper;

/**
 * 监听外部消息并i解析、选择相应的事件流程启动流程的外部接口类
 * @author Administrator
 *
 */
@Path("/message")
public class MessageHandler implements MessageListener {
	 
	
	private static ExecutorService mhPool = null;   
	private static Logger log = Logger.getLogger(MessageHandler.class);
	private static Map<String, Event> eventMap = new HashMap<String, Event>(); 
	public static final String HEAD_PROPERTY = "head";
	public static final String EVETN_PROPERTY = "event";
	public static final String BATCH_PROPERTY = "batch"; 
	public static final String PARAM_PROPERTY = "param"; 
	public static final  int BATCH_FLAG = 1; 
	
	
	static{
		PropertiesConfiguration config;
		int poolNum = Constants.EVENT_POOL_NUM_DEFAULT;
		try {
			config = new PropertiesConfiguration(Constants.CONFIG_FILE);
			poolNum = config.getInt(Constants.EVENT_POOL_NUM_KEY, Constants.EVENT_POOL_NUM_DEFAULT);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		mhPool = Executors.newFixedThreadPool(poolNum);
		EventConfigHelper.initEventFromXml();
		 
//		Event event = new Event(); 
//		event.setName("testevent"); 
//		event.setParameter("userparam");
//		
//		event.setStartAction("action1");
//		
//		RestAction action1 = new RestAction("action1", "获取调度信息");
//		action1.setResultKey("action1result");
//		action1.setUrl("${userparam.head.url}");
//		action1.setParameter("${userparam.param}");
//		Validate v = new Validate("action2");
//		v.addOperation(new EQOperation("1", "result"));
//		action1.addValidate(v);
//		
//		RestAction action2 = new RestAction("action2", "获取其他信息"); 
//		action2.setResultKey("action2result");
//		action2.setUrl("${userparam.head.url}");
//		action2.setParameter("${action1result}");
//		Validate v1 = new Validate("action3");
//		v1.addOperation(new EQOperation("1", "result"));
//		action2.addValidate(v1);
//		
//		RestAction action3 = new RestAction("action3", "中和所有信息"); 
//		action3.setResultKey("action3result");
//		action3.setUrl("${userparam.head.url}");
//		action3.setParameter("${action2result}");
//		Validate v2 = new Validate("end");
//		v2.addOperation(new EQOperation("1", "result"));
//		action3.addValidate(v2);
//		
//		event.addAction(action1);
//		event.addAction(action2);
//		event.addAction(action3);
//		event.setResult("${action3result}");
//		
//		eventMap.put(event.getName(), event); 
	} 
	/**
	 * 
	 * @param obj
	 * @return
	 */
	@POST
	@Produces(value={"text/xml", "application/json"})
	public static String onMessage(JSONObject obj){  
		//获取当前需要出发的事件对象
		Event evnet = getEvent(obj.getJSONObject(HEAD_PROPERTY).getString(EVETN_PROPERTY));
		//是否是批处理事件
		int batchFlag = obj.getJSONObject(HEAD_PROPERTY).getInt(BATCH_PROPERTY); 
		//如果为批处理任务
		if(batchFlag == BATCH_FLAG){
			Object param = obj.get(PARAM_PROPERTY);
			if(param.getClass().isAssignableFrom(JSONArray.class)){
				 //如果当前使用批处理，而且参数时数组
				JSONArray params = (JSONArray)(param);  
				//生成子事件
				for(int i = 0; i < params.size(); i++){
					JSONObject eventParam = new JSONObject();
					eventParam.put(HEAD_PROPERTY, obj.getJSONObject(HEAD_PROPERTY));
					eventParam.put(PARAM_PROPERTY, params.getJSONObject(i));
//					Process task = new Process(evnet, eventParam);  
					startProcess(new Process(evnet, eventParam));
				}
			}else{
				//如果参数不是数组只出发一个事件
				startProcess(new Process(evnet, obj));
			} 
		}else{
			startProcess(new Process(evnet, obj));
		}
		return null; 
	} 
	/*
	 * 开始一个任务流程
	 */
	public static void startProcess(Process task){
		 try {    
	        	mhPool.submit(task);   
	        } catch (Exception e){   
	        	e.printStackTrace();
	        	log.error(e.toString()); 
	        }  
	}
	
	public static Event getEvent(String eventKey){
		return eventMap.get(eventKey); 
	} 
	
	public static void putEvent(String key, Event event){
		eventMap.put(key, event);
	}
	
	public static void addEvent(Event event){ 
		eventMap.put(event.getName(), event);
	}
	
	public static void main(String[] args) {
		JSONObject obj1 = JSONObject.fromObject(("{\"head\": {\"url\": \"testulr\"},\"param\": {\"dep\": \"1\"}}"));
		String a = obj1.toString().replaceAll("\"", "\\\\\\\"");
		System.out.println( a);
//		onMessage(
//		JSONObject obj = JSONObject.fromObject("{\"head\": {\"event\": \"event1\",\"user\":\"lilin\", \"url\": \"testurl\"},\"param\": \"" obj1.toString().replaceAll("\"", "\\\\\\\"")+ obj1.toString().replaceAll("\"", "\\\\\\\"") + "\"}");
//		System.out.println(obj);
//		System.out.println(obj.get(PARAM_PROPERTY).getClass().isAssignableFrom(JSONArray.class));
		//		onMessage("{\"head\": {\"user\":\"lilin\", \"url\": \"testurl\"},\"param\": {\"task\": \"task2\",\"dep\": 1}}");
	}
    /**
     * mq消息监听函数，从mq中接收消息并把消息格式化以后发送给处理函数
     */
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		log.info("get the message from rest:");
		log.info(message);   
		try{
			 JSONObject json = JSONObject.fromObject(new String(message.getBody()));
			 MessageHandler.onMessage(json); 
		}catch(Exception e){
			log.error("接受消息错误:", e);
			return;
		} 
	}
}

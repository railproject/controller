package com.crec.controller.event.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import com.crec.controller.engine.InnerMessageHandler;
import com.crec.controller.engine.MessageHandler;
import com.crec.controller.event.validate.Validate;
import com.crec.controller.utile.Constants;
import com.crec.controller.utile.ParameterHelper;
import com.crec.controller.utile.SpringBeanUtils;
/**
 * 任务流程类每个任务都喊一个事件，实践中包含了这个流程需要执行的具体要操作的基本信息
 * @author Administrator
 *
 */
public class Process implements Callable<Object>{
	
	private static Logger log = Logger.getLogger(MessageHandler.class);
	
	private String uuid;
	private Event event;   
	private String username;
	//组任务的基本信息类，目前没有启用
	private GroupInfo groupInfo; 

	@Autowired
	private  AmqpTemplate replyTemplate; 
	//资源池，这个会把用户参数，和某个操作的结果都放到这个map中
	private Map<String, JSONObject> resources = new HashMap<String, JSONObject>() ;  
	
	public Process(Event event, JSONObject param){
		this.event = event;
		this.currentAction = event.getStartAction();
		this.setUsername(param.getJSONObject(MessageHandler.HEAD_PROPERTY).getString("user")); 
		this.resources.put(event.getParameter(), param.getJSONObject(MessageHandler.PARAM_PROPERTY)); 
		this.uuid = param.getJSONObject(MessageHandler.HEAD_PROPERTY).getString("requestId");
	}
	/**
	 * 当需要使用组信息的时候就可以使用该构造函数，目前还未使用到组的 方式
	 * @param event
	 * @param groupInfo
	 * @param param
	 */
	public Process(Event event, GroupInfo groupInfo, JSONObject param){
		this.event = event;
		this.currentAction = event.getStartAction();
		this.setUsername(param.getJSONObject(MessageHandler.HEAD_PROPERTY).getString("user")); 
		this.resources.put(event.getParameter(), param.getJSONObject(MessageHandler.PARAM_PROPERTY)); 
		this.uuid = param.getJSONObject(MessageHandler.HEAD_PROPERTY).getString("requestId");
		this.groupInfo = groupInfo;
	} 

	private String currentAction; 
	 
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	} 
	//以JSON格式的凡是返回当前资源池中的所有内容
	public JSONObject getResource(){
		JSONObject json = new JSONObject();
		 if (this.resources != null && this.resources.size() > 0) {
	            for (String key : this.resources.keySet()) {
	            	json.put(key, this.resources.get(key));
	            } 
	     } 
		 return json;
	}
	
	public String getResultParam(){
		return this.event.getResult();
	}  
	
	public String getCurrentAction() {
		return currentAction;
	}

	public void setCurrentAction(String currentAction) {
		this.currentAction = currentAction;
	}  
	
	public void putResult(String resultKey,JSONObject result){
		resources.put(resultKey, result); 
	}
	
	public AbstractAction getAction(String name){
		return  this.event.getAction(name);
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	/**
	 * 完成具体炒作的函数
	 */
	public Object call() throws Exception {
		if("END".equals(this.currentAction)){
			this.sendResult();
			return null;
		}
		AbstractAction action = this.event.getAction(this.currentAction); 
		log.debug("proccess:[" + this.uuid + "]running event[" + this.event.getName() + "] [" + action.getDescription() + "]start");
		JSONObject result = action.doAction(this); 
		
		log.info("proccess:[" + this.uuid + "]running event[" + this.event.getName() + "] [" + action.getDescription() + "]getresult");
		log.info(result.toString());
		//把当前的执行结果放到资源池中
		this.putResult(action.getResultKey(), result);  
		ArrayList<Validate>  arr = action.getValidates();
		//如果一个单人分成多任务这个地方会出错吧
		boolean flag = false;
		for(Validate v : arr){
			if(v.validate(result)){//如果验证通过设置下一个需要执行的操作ID
				this.setCurrentAction(v.getNext());
				log.debug("验证通过:[" + action.getDescription() + "]");
				//把他放到线程池等待执行
				InnerMessageHandler.fireEvent(this);
				flag = true;
				break;
			} 
		} 
		//处理异常
		if(!flag){
			this.exceptionHandle();
		}
		log.debug("proccess:[" + this.uuid + "]run event[" + this.event.getName() + "] [" + action.getDescription() + "]end");
		return null;
	}
	/**
	 * 返回给用户的结果函数
	 */ 
	public void sendResult(){
		log.debug("proccess:[" + this.uuid + "]返回用户需要的结果:");
		if(this.groupInfo != null){
			this.groupInfo.setSuccessNum();
		}
		String resultParam = this.getResultParam();
		log.debug("======="  +  resultParam);
		if(resultParam != null && !"".equals(resultParam)){
//			this.getAmqpTemp().convertAndSend(this.getRoutingKey(), "{\"reuqestId\": \"" + this.uuid + "\"}".getBytes());// send
			StringBuffer result =  new StringBuffer("{\"reuqestId\": \"")
								.append(this.uuid)
								.append("\", \"result\":{\"code\": \"0\",\"userparam\":\"")
								.append(this.resources.get(this.event.getParameter()).toString().replaceAll("\"", "\\\\\\\"")).append("\"")
								.append(",\"result\":\"")
								.append(ParameterHelper.getReplacement(this.getResultParam(), this.getResource()).replaceAll("'", "\"").replaceAll("\"", "\\\\\\\""))
								.append("\"");
			if(this.groupInfo != null){
				result.append(",\"groupInfo\":{\"currentNum\":").append(this.groupInfo.getCurrentNum())
				.append("\",successNum\":").append(this.groupInfo.getSuccessNum())
				.append("\",errorNum\":").append(this.groupInfo.getErrNum())
				.append("\",eventCount\":").append(this.groupInfo.getEventCount()).append("}}}");
			}else{
				result.append("}}");
			} 				
			this.getAmqpTemp().convertAndSend(this.getRoutingKey(), result.toString().getBytes()); 
			log.info("send message to :" + this.getRoutingKey());	
			log.info("result message:" + result);	
		}
	} 
	
	public  AmqpTemplate getAmqpTemp(){
		return (AmqpTemplate)SpringBeanUtils.getBean(this.event.getAmqpTemp());
	}
	
	public String getRoutingKey(){
		return this.event.getRoutingKey() != null && !"".equals(this.event.getRoutingKey()) ? this.event.getRoutingKey() : Constants.EVEVT_RESULT_ROUTINGKEY + this.event.getName() ;
	} 
	/**
	 * 发生异常时调用的结果返回函数
	 */
	public void exceptionHandle(){ 
		if(this.groupInfo != null){
			this.groupInfo.setErrNum();
		}
		AbstractAction action = this.event.getAction(this.currentAction);   
		log.debug("proccess:[" + this.uuid + "]当前处理遇到错误:[" + action.getDescription() + "]");  
		log.debug("proccess:[" + this.uuid + "]处理结果:[" + this.resources.get(action.getResultKey()) + "]");
		StringBuffer result =  new StringBuffer("{\"reuqestId\": \"")
							.append(this.uuid)
							.append("\",\"result\":{\"code\":\"-1\", \"message\":\"处理遇到错误:[")
							.append(action.getDescription()).append("]\",\"userparam\":\"")
							.append(this.resources.get(this.event.getParameter()).toString().replaceAll("\"", "\\\\\\\""))
							.append("\"");
		if(this.groupInfo != null){
			result.append(",\"groupInfo\":{\"currentNum\":").append(this.groupInfo.getCurrentNum())
			.append("\",successNum\":").append(this.groupInfo.getSuccessNum())
			.append("\",errorNum\":").append(this.groupInfo.getErrNum())
			.append("\",eventCount\":").append(this.groupInfo.getEventCount()).append("}}}");
		}else{
			result.append("}}");
		} 	
		this.getAmqpTemp().convertAndSend(this.getRoutingKey(), result.toString().getBytes());
		log.debug("send message to :" + this.getRoutingKey());	
		log.debug("result message:" + result);	
	}

}

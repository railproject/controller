package com.crec.controller.event.validate;

import java.util.ArrayList;
/**
 * 逻辑计算表达式的超类
 * @author Administrator
 *
 */
public abstract class SymbolOperation {
	
	protected  ArrayList<Operation> operations = new ArrayList<Operation>();
	 
	
	public  void addOperation(Operation operation){
		this.operations.add(operation);
	}
	public static void main(String[] args) {
		System.out.println();
	}
}

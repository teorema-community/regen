package br.inf.teorema.regen.enums;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;

public enum FunctionType {	
	ABS, AVG, COUNT, COUNT_DISTINCT, DIFF, LOWER, MAX, MIN, MOD, NEG, PROD, QUOT, SQRT, SUM, SUBSTRING, TRIM, UPPER;	
	
	private String methodName;
	
	private FunctionType() {}
	
	private FunctionType(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodName() {
		if (methodName == null) {
			methodName = this.toString().toLowerCase();
			
			while (methodName.contains("_")) {
				int index = methodName.indexOf("_");
				methodName = methodName.substring(0, index) + methodName.substring(index + 1, index + 2).toUpperCase() + methodName.substring(index + 2);
			}
		}
		
		return methodName;
	}
	
	public static FunctionType getByMethodName(String name, boolean throwExceptionIfNotFound) {
		if (name != null && !name.isEmpty()) {
			for (FunctionType type : values()) {
				if (name.equalsIgnoreCase(type.getMethodName())) {
					return type;
				}
			}
		}
		
		if (throwExceptionIfNotFound) {
			throw new NullPointerException("Function " + name + " not found");
		}
		
		return null;
	}
	
	/*public static void main(String[] args) {
		System.out.println(COUNT_DISTINCT.getMethodName());
		System.out.println(getByMethodName("sdfdsf", true));
	}*/
}

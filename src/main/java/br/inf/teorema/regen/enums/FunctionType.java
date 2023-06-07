package br.inf.teorema.regen.enums;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;

public enum FunctionType {	
	ABS, AVG, COUNT, COUNT_DISTINCT("countDistinct"), DIFF, LOWER, MAX, MIN, MOD, NEG, PROD, QUOT, SQRT, SUM, SUBSTRING, TRIM, UPPER;	
	
	private String methodName;
	
	private FunctionType() {}
	
	private FunctionType(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodName() {
		if (methodName == null) {
			methodName = this.toString().toLowerCase();
		}
		
		return methodName;
	}
}

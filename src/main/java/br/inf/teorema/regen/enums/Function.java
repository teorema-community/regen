package br.inf.teorema.regen.enums;

import java.util.Arrays;

import br.inf.teorema.regen.model.FunctionField;

public enum Function {
	ABS, AVG, COUNT, COUNT_DISCTINT, DIFF, LOWER, MAX, MIN, MOD, NEG, PROD, QUOT, SQRT, SUM, SUBSTRING, TRIM, UPPER;
	
	public static FunctionField extract(String projection) {
		if (hasFunction(projection)) {
			FunctionField functionField = new FunctionField();
			String funStr = projection.substring(0, projection.indexOf("("));
			functionField.setFunction(Function.valueOf(funStr.toUpperCase()));
			String parameters = projection.replace(funStr, "").replace("(", "").replace(")", "");
			functionField.setParameters(Arrays.asList(parameters.split(",")));
			
			return functionField;
		}
		
		return null;
	}
	
	public static boolean hasFunction(String projection) {
		return projection != null && projection.contains("(");
	}
}

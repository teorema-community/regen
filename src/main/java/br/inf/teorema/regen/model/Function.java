package br.inf.teorema.regen.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.inf.teorema.regen.enums.FunctionType;
import br.inf.teorema.regen.util.JSONUtil;

public class Function {
	
	private FunctionType type;
	private String content;
	private List<Function> subFunctions;
	
	public Function(FunctionType type) {
		super();
		this.type = type;
	}

	public static Function extractFunctionFromfield(String field) {		
		if (field != null && !field.isEmpty()) {			
			int startIndex = field.indexOf("(");
			
			if (startIndex > -1 ) {
				String functionName = field.substring(0, startIndex).trim();
				Function function = new Function(FunctionType.valueOf(functionName.toUpperCase()));
				
				int endIndex = field.lastIndexOf(")");
				
				if (endIndex == -1) {
					endIndex = field.length();
				}
				
				String temp = field.substring(startIndex + 1, endIndex);			
				
				if (!temp.isEmpty()) {
					String[] fields = temp.split(",");
					
					for (String f : fields) {
						Function subFunction = extractFunctionFromfield(f);
						
						if (subFunction != null) {
							function.getSubFunctions().add(subFunction);
						} else {
							function.setContent(temp);
						}
					}
				}
				
				return function;
			}
		}
		
		return null;
	}
	
	public static void main(String[] args) throws JsonProcessingException {
		System.out.println(JSONUtil.prettify(extractFunctionFromfield("diff(sum(balances.entries), sum(balances.exits))")));
	}
	
	public FunctionType getType() {
		return type;
	}
	public void setType(FunctionType type) {
		this.type = type;
	}
	public List<Function> getSubFunctions() {
		if (subFunctions == null) {
			subFunctions = new ArrayList<>();
		}
		
		return subFunctions;
	}
	public void setSubFunctions(List<Function> subFunctions) {
		this.subFunctions = subFunctions;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
}

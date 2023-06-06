package br.inf.teorema.regen.model;

import java.util.ArrayList;
import java.util.List;

import br.inf.teorema.regen.enums.FunctionType;

public class Function {
	
	private FunctionType type;
	private List<Function> subFunctions;
	
	public Function(FunctionType type) {
		super();
		this.type = type;
	}

	public static List<Function> extractFunctionsFromfield(String field) {
		List<Function> functions = new ArrayList<>();
		
		if (field != null && !field.isEmpty()) {
			String[] fields = field.split(",");
			
			for (String f : fields) {
				int startIndex = field.indexOf("(");
				
				if (startIndex > -1 ) {
					String functionName = f.substring(0, startIndex);
					Function function = new Function(FunctionType.valueOf(functionName.toUpperCase()));
					
					int endIndex = f.lastIndexOf(")");
					
					if (endIndex == -1) {
						endIndex = f.length();
					}
					
					String content = f.substring(startIndex + 1, endIndex);
					function.setSubFunctions(extractFunctionsFromfield(content));
					functions.add(function);
				}
			}
		}
		
		return functions;
	}
	
	public static void main(String[] args) {
		System.out.println(extractFunctionsFromfield("diff(sum(balances.entries), sum(balances.entries))"));
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
	
}

package br.inf.teorema.regen.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.inf.teorema.regen.enums.FunctionType;
import br.inf.teorema.regen.specification.GenericSpecification;
import br.inf.teorema.regen.util.JSONUtil;

public class Function {
	
	private FunctionType type;
	private String content;
	private List<Function> subFunctions;
	private Expression expression;
	
	public Function(FunctionType type) {
		super();
		this.type = type;	
	}
	
	private List<FieldExpression> createExpression(
		GenericSpecification<?> genericSpecification, JoinType joinType, List<FieldJoin> fieldJoins, From<?, ?> from, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder
	) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, ParseException {
		List<FieldExpression> subExpressions = new ArrayList<>();
		
		if (type != null) {			
			if (getSubFunctions().isEmpty()) {
				if (getContent() != null && !getContent().isEmpty()) {
					subExpressions.add(
						genericSpecification.getFieldExpressionByField(getContent(), joinType, fieldJoins, from, query, criteriaBuilder)
					);
				}
			} else {
				for (Function subFunction : getSubFunctions()) {
					subExpressions.addAll(subFunction.createExpression(genericSpecification, joinType, fieldJoins, from, query, criteriaBuilder));
				}
			}
			
			if (!subExpressions.isEmpty()) {
				List<Class<?>> fieldTypes = subExpressions.stream().map(fe -> fe.getFieldType()).collect(Collectors.toList());
				Method method = criteriaBuilder.getClass().getMethod(
					this.type.getMethodName(), fieldTypes.toArray(new Class<?>[fieldTypes.size()])
				);
				this.setExpression((Expression) method.invoke(
					criteriaBuilder,
					subExpressions.stream().map(fe -> fe.getExpression()).collect(Collectors.toList())
				));
			}
		}
		
		return subExpressions;
	}

	public static Function extractFunctionFromfield(
		String field, GenericSpecification<?> genericSpecification, JoinType joinType, List<FieldJoin> fieldJoins, From<?, ?> from, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder
	) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, ParseException {		
		if (field != null && !field.isEmpty()) {			
			int startIndex = field.indexOf("(");
			
			if (startIndex > -1 ) {
				String functionName = field.substring(0, startIndex).trim();
				Function function = new Function(
					FunctionType.valueOf(functionName.toUpperCase())
				);
				function.createExpression(genericSpecification, joinType, fieldJoins, from, query, criteriaBuilder);	
				
				int endIndex = field.lastIndexOf(")");
				
				if (endIndex == -1) {
					endIndex = field.length();
				}
				
				String temp = field.substring(startIndex + 1, endIndex);			
				
				if (!temp.isEmpty()) {
					String[] fields = temp.split(",");
					
					for (String f : fields) {
						Function subFunction = extractFunctionFromfield(f, genericSpecification, joinType, fieldJoins, from, query, criteriaBuilder);
						
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
	
	/*public static void main(String[] args) throws JsonProcessingException {
		System.out.println(JSONUtil.prettify(extractFunctionFromfield("diff(sum(balances.entries), sum(balances.exits))")));
	}*/
	
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

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}
	
}

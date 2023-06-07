package br.inf.teorema.regen.model;

import java.lang.reflect.Method;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;

public class FieldExpression {

    private Expression expression;
    private Class<?> fieldType;
    private String fieldName;
    private Function function;

    public FieldExpression() {}

    public FieldExpression(Expression expression, Class<?> fieldType, String fieldName) {
        this.expression = expression;
        this.fieldType = fieldType;
        this.fieldName = fieldName;
    }

    public FieldExpression(Function function) {
		super();
		this.function = function;
		
		if (function != null) {
			this.expression = function.getExpression();
			
			if (function.getExpression() != null) {
				this.fieldType = function.getExpression().getClass();
			}
		}
	}

	public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }

    public void setFieldType(Class<?> fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

	public Function getFunction() {
		return function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}
	
	public boolean isFunction() {
		return function != null;
	}

}

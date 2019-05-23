package br.inf.teorema.regen.model;

import javax.persistence.criteria.Expression;

public class FieldExpression {

    private Expression expression;
    private Class<?> fieldType;
    private String fieldName;

    public FieldExpression() {}

    public FieldExpression(Expression expression, Class<?> fieldType, String fieldName) {
        this.expression = expression;
        this.fieldType = fieldType;
        this.fieldName = fieldName;
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

}

package br.inf.teorema.regen.model;

import br.inf.teorema.regen.constants.ConditionalOperator;
import br.inf.teorema.regen.constants.LogicalOperator;

public class Condition {

	private String field;
	private LogicalOperator logicalOperator = LogicalOperator.AND;
	private ConditionalOperator conditionalOperator = ConditionalOperator.EQUALS;
	private Object value;
	
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public LogicalOperator getLogicalOperator() {
		return logicalOperator;
	}
	public void setLogicalOperator(LogicalOperator logicalOperator) {
		this.logicalOperator = logicalOperator;
	}
	public ConditionalOperator getConditionalOperator() {
		return conditionalOperator;
	}
	public void setConditionalOperator(ConditionalOperator conditionalOperator) {
		this.conditionalOperator = conditionalOperator;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
}

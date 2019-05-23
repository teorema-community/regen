package br.inf.teorema.regen.model;

import br.inf.teorema.regen.constants.ConditionalOperator;
import br.inf.teorema.regen.constants.LogicalOperator;

import java.util.ArrayList;
import javax.persistence.criteria.JoinType;
import java.util.List;

public class Condition {

	private LogicalOperator logicalOperator = LogicalOperator.AND;
	private String field;
	private JoinType joinType = JoinType.INNER;
	private List<FieldJoin> fieldJoins;
	private ConditionalOperator conditionalOperator = ConditionalOperator.EQUALS;
	private Object value;
	private String fieldValue;
	private List<Condition> conditions;
	
	public Condition() {}

	public Condition(String field) {
		this.field = field;
	}

	public Condition(String field, ConditionalOperator conditionalOperator, Object value) {
		super();
		this.field = field;
		this.conditionalOperator = conditionalOperator;
		this.value = value;
	}

	public Condition(String field, Object value) {
		super();
		this.field = field;
		this.value = value;
	}

	public Condition(LogicalOperator logicalOperator) {
		super();
		this.logicalOperator = logicalOperator;
	}

	public LogicalOperator getLogicalOperator() {
		return logicalOperator;
	}
	public void setLogicalOperator(LogicalOperator logicalOperator) {
		this.logicalOperator = logicalOperator;
	}

	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}

	public JoinType getJoinType() {
		if (joinType == null) {
			joinType = JoinType.INNER;
		}

		return joinType;
	}

	public List<FieldJoin> getFieldJoins() {
		if (fieldJoins == null) {
			fieldJoins = new ArrayList<>();
		}

		return fieldJoins;
	}

	public void setFieldJoins(List<FieldJoin> fieldJoins) {
		this.fieldJoins = fieldJoins;
	}

	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
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

	public String getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	public List<Condition> getConditions() {
		if (conditions == null) {
			conditions = new ArrayList<Condition>();
		}
		
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}
	
	public void addCondition(Condition condition) {
		this.getConditions().add(condition);
	}
	
	public void addCondition(String field, ConditionalOperator conditionalOperator, Object value) {
		this.addCondition(new Condition(field, conditionalOperator, value));
	}
	
	public void addCondition(String field, Object value) {
		this.addCondition(new Condition(field, value));
	}

	public void addFieldJoin(FieldJoin fieldJoin) {
		this.getFieldJoins().add(fieldJoin);
	}

	public void addFieldJoin(String field, String onSourceConditionField, String onConditionField) {
		this.addFieldJoin(new FieldJoin(field, onSourceConditionField, onConditionField));
	}

	public void addFieldJoin(String joinField, JoinType type, String onSourceConditionField, String onConditionField) {
		this.addFieldJoin(new FieldJoin(joinField, type, onSourceConditionField, onConditionField));
	}

	public void addFieldJoin(String joinSourceField, String joinField, String onSourceConditionField, String onConditionField) {
		this.addFieldJoin(new FieldJoin(joinSourceField, joinField, onSourceConditionField, onConditionField));
	}

	public void addFieldJoin(String joinSourceField, String joinField, JoinType type, String onSourceConditionField, String onConditionField) {
		this.addFieldJoin(new FieldJoin(joinSourceField, joinField, type, onSourceConditionField, onConditionField));
	}
	
}

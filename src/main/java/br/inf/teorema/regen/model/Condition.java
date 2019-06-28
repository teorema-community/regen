package br.inf.teorema.regen.model;

import br.inf.teorema.regen.constants.ConditionalOperator;
import br.inf.teorema.regen.constants.LogicalOperator;

import javax.persistence.criteria.JoinType;
import java.util.ArrayList;
import java.util.List;

public class Condition {

	private LogicalOperator logicalOperator = LogicalOperator.AND;
	private String field;
	private JoinType joinType = JoinType.INNER;
	private List<FieldJoin> fieldJoins;
	private ConditionalOperator conditionalOperator = ConditionalOperator.EQUALS;
	private Object value;
	private String expressionValue;
	private List<Condition> conditions;
	private List<String> groupBy;
	private List<Condition> orderBy;
	private List<Case> cases;
	
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

	public String getExpressionValue() {
		return expressionValue;
	}

	public void setExpressionValue(String expressionValue) {
		this.expressionValue = expressionValue;
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

	public void addFieldJoin(String field) {
		this.addFieldJoin(new FieldJoin(field));
	}

	public void addFieldJoin(String field, JoinType type) {
		this.addFieldJoin(new FieldJoin(field, type));
	}

	public void addFieldJoin(String sourceField, String field) {
		this.addFieldJoin(new FieldJoin(sourceField, field));
	}

	public void addFieldJoin(String sourceField, String field, JoinType type) {
		this.addFieldJoin(new FieldJoin(sourceField, field, type));
	}

    public List<String> getGroupBy() {
	    if (groupBy == null) {
	        groupBy = new ArrayList();
        }

        return groupBy;
    }

    public void setGroupBy(List<String> groupBy) {
        this.groupBy = groupBy;
    }

	public List<Condition> getOrderBy() {
		if (orderBy == null) {
			orderBy = new ArrayList<>();
		}

		return orderBy;
	}

	public void setOrderBy(List<Condition> orderBy) {
		this.orderBy = orderBy;
	}

	public List<Case> getCases() {
		if (cases == null) {
			cases = new ArrayList<>();
		}

		return cases;
	}

	public void setCases(List<Case> cases) {
		this.cases = cases;
	}
}

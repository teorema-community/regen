package br.inf.teorema.regen.model;

import javax.persistence.criteria.JoinType;

import br.inf.teorema.regen.enums.ConditionalOperator;
import br.inf.teorema.regen.enums.LogicalOperator;
import br.inf.teorema.regen.enums.OrderDirection;

import java.util.ArrayList;
import java.util.Arrays;
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
	private List<OrderBy> orderBies;
	private List<Case> cases;
	private Boolean distinct = false;
	private Boolean not = false;
	
	public Condition() {}
	
	public Condition(Condition... conditions) {
		this.addCondition(conditions);
	}

	public Condition(String field) {
		this.field = field;
	}
	
	public Condition(String field, Condition... conditions) {
		this.field = field;
		this.addCondition(conditions);
	}

	public Condition(String field, ConditionalOperator conditionalOperator, Object value) {
		super();
		this.field = field;
		this.conditionalOperator = conditionalOperator;
		this.value = value;
	}
	
	public Condition(String field, ConditionalOperator conditionalOperator, Object value, Condition... conditions) {
		super();
		this.field = field;
		this.conditionalOperator = conditionalOperator;
		this.value = value;
		this.addCondition(conditions);
	}

	public Condition(String field, Object value) {
		super();
		this.field = field;
		this.value = value;
	}
	
	public Condition(String field, Object value, Condition... conditions) {
		super();
		this.field = field;
		this.value = value;
		this.addCondition(conditions);
	}
	
	public Condition(String field, ConditionalOperator conditionalOperator, Object... values) {
		super();
		this.field = field;
		this.conditionalOperator = conditionalOperator;
		
		if (values != null) {
			this.value = Arrays.asList(values);
		}
	}

	public Condition(LogicalOperator logicalOperator) {
		super();
		this.logicalOperator = logicalOperator;
	}
	
	public Condition(LogicalOperator logicalOperator, Condition... conditions) {
		super();
		this.logicalOperator = logicalOperator;
		this.addCondition(conditions);
	}
	
	public static Condition isNotNull(String field) {
		Condition condition = new Condition(field);
		condition.setConditionalOperator(ConditionalOperator.IS_NOT_NULL);
		
		return condition;
	}
	
	public static Condition isNull(String field) {
		Condition condition = new Condition(field);
		condition.setConditionalOperator(ConditionalOperator.IS_NULL);
		condition.setJoinType(JoinType.LEFT);
		
		return condition;
	}
	
	public static Condition equals(String field, Object value) {
		return new Condition(field, ConditionalOperator.EQUALS, value);
	}
	
	public static Condition notEquals(String field, Object value) {
		return new Condition(field, ConditionalOperator.NOT_EQUALS, value);
	}
	
	public static Condition greaterThan(String field, Object value) {
		return new Condition(field, ConditionalOperator.GREATER_THAN, value);
	}
	
	public static Condition lessThan(String field, Object value) {
		return new Condition(field, ConditionalOperator.LESS_THAN, value);
	}
	
	public static Condition greaterThanOrEqualTo(String field, Object value) {
		return new Condition(field, ConditionalOperator.GREATER_THAN_OR_EQUAL_TO, value);
	}
	
	public static Condition lessThanOrEqualTo(String field, Object value) {
		return new Condition(field, ConditionalOperator.LESS_THAN_OR_EQUAL_TO, value);
	}
	
	public static Condition like(String field, Object value) {
		return new Condition(field, ConditionalOperator.LIKE, value);
	}
	
	public static Condition likeStart(String field, Object value) {
		return new Condition(field, ConditionalOperator.LIKE_START, value);
	}
	
	public static Condition likeEnd(String field, Object value) {
		return new Condition(field, ConditionalOperator.LIKE_END, value);
	}
	
	public static Condition customLike(String field, Object value) {
		return new Condition(field, ConditionalOperator.CUSTOM_LIKE, value);
	}
	
	public static Condition between(String field, Object... values) {
		return new Condition(field, ConditionalOperator.BETWEEN, values);
	}
	
	public static Condition in(String field, Object... values) {
		return new Condition(field, ConditionalOperator.IN, values);
	}
	
	public Condition leftJoin() {
		this.setJoinType(JoinType.LEFT);
		return this;
	}
	
	public Condition innerJoin() {
		this.setJoinType(JoinType.INNER);
		return this;
	}
	
	public Condition rightJoin() {
		this.setJoinType(JoinType.RIGHT);
		return this;
	}
	
	public Condition distinct() {
		this.setDistinct(true);
		return this;
	}
	
	public Condition not() {
		this.setNot(true);
		return this;
	}
	
	public Condition groupBy(String field) {
		this.getGroupBy().add(field);
		return this;
	}
	
	public Condition orderBy(OrderBy orderBy) {
		this.getOrderBies().add(orderBy);
		return this;
	}
	
	public Condition orderBy(OrderDirection direction, Condition condition) {
		return orderBy(new OrderBy(direction, condition));
	}
	
	public Condition orderBy(OrderDirection direction, String field) {
		return orderBy(new OrderBy(direction, field));
	}
	
	public Condition orderBy(String field) {
		return orderBy(new OrderBy(field));
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
	
	public void addCondition(Condition... conditions) {
		this.getConditions().addAll(Arrays.asList(conditions));
	}
	
	public void addCondition(String field, ConditionalOperator conditionalOperator, Object value) {
		this.addCondition(new Condition(field, conditionalOperator, value));
	}
	
	public void addCondition(String field, ConditionalOperator conditionalOperator, Object... values) {
		this.addCondition(new Condition(field, conditionalOperator, values));
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

	public List<OrderBy> getOrderBies() {
		if (orderBies == null) {
			orderBies = new ArrayList<>();
		}

		return orderBies;
	}

	public void setOrderBies(List<OrderBy> orderBies) {
		this.orderBies = orderBies;
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

	public Boolean getDistinct() {
		if (distinct == null) {
			distinct = false;
		}
		
		return distinct;
	}

	public void setDistinct(Boolean distinct) {
		this.distinct = distinct;
	}

	public Boolean getNot() {
		return not;
	}

	public void setNot(Boolean not) {
		this.not = not;
	}
	
}

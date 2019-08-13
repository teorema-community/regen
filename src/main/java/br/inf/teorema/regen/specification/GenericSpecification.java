package br.inf.teorema.regen.specification;

import br.inf.teorema.regen.enums.ConditionalOperator;
import br.inf.teorema.regen.enums.LogicalOperator;
import br.inf.teorema.regen.enums.OrderDirection;
import br.inf.teorema.regen.model.*;
import br.inf.teorema.regen.util.DateUtils;
import br.inf.teorema.regen.util.ReflectionUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;

@SuppressWarnings("serial")
public class GenericSpecification<T> implements Specification<T> {

	private Condition condition;
	private Class<?> clazz;
	private List<FieldJoin> joins;

	public GenericSpecification(Condition condition, Class<?> clazz) {
		super();
		this.condition = condition;
		this.clazz = clazz;
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		try {
			resetJoins();
			query = setGroupBy(root, query, criteriaBuilder);
			query = setOrderBy(root, query, criteriaBuilder);

			return addCondition(this.condition, LogicalOperator.AND, new ArrayList<Predicate>(), true, root, query, criteriaBuilder).get(0);
		} catch (NoSuchFieldException | ParseException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}

	private List<Predicate> addCondition(
			Condition condition, LogicalOperator logicalOperator, List<Predicate> predicates, boolean last, Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder
	) throws NoSuchFieldException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		if (!condition.getConditions().isEmpty()) {
			List<Predicate> tempPredicates = new ArrayList<>();

			int i = 0;
			for (Condition subCondition : condition.getConditions()) {
				tempPredicates = addCondition(subCondition, condition.getLogicalOperator(), tempPredicates, i >= condition.getConditions().size() - 1, root, query, criteriaBuilder);
				i++;
			}

			predicates.addAll(tempPredicates);
		}

		if (logicalOperator != null
				&& condition.getField() != null
				&& condition.getConditionalOperator() != null
				&& (
					(
						condition.getValue() != null
						&& !condition.getValue().toString().isEmpty()
					)
					|| (
						condition.getExpressionValue() != null
						&& !condition.getExpressionValue().isEmpty()
					)
					|| condition.getConditionalOperator().equals(ConditionalOperator.IS_NOT_NULL)
					|| condition.getConditionalOperator().equals(ConditionalOperator.IS_NULL)
				)
		) {
			FieldExpression fieldExpression = getFieldExpressionByCondition(condition, root, query, criteriaBuilder);
			Object value = condition.getValue();
			boolean isValueExpression = false;

			if (condition.getExpressionValue() != null) {
				isValueExpression = true;
				value = this.getFieldExpressionByField(condition.getExpressionValue(), condition.getJoinType(), condition.getFieldJoins(), root).getExpression();
			} else if (fieldExpression.getFieldType().isEnum() && value != null && !(value instanceof Enum<?>)) {
				value = fieldExpression.getFieldType().getDeclaredMethod("valueOf", String.class).invoke(null, value.toString());
			}

			predicates.add(createPredicate(
				fieldExpression, condition.getConditionalOperator(), value, isValueExpression, criteriaBuilder
			));
		}

		if (last && logicalOperator != null) {
			Predicate predicate = joinPredicates(predicates, logicalOperator, criteriaBuilder);
			predicates = new ArrayList<>();
			predicates.add(predicate);
		}

		return predicates;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Predicate createPredicate(
		FieldExpression fieldExpression, ConditionalOperator conditionalOperator, Object value, boolean isValueExpression, CriteriaBuilder criteriaBuilder
	) throws ParseException {
		switch (conditionalOperator) {
			case EQUALS:
				if (!isValueExpression && fieldExpression.getFieldType().equals(Date.class)) {
					return criteriaBuilder.equal(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
				} else if (!isValueExpression && fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.equal(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else {
					return criteriaBuilder.equal(fieldExpression.getExpression(), value);
				}
			case NOT_EQUALS:
				if (!isValueExpression && fieldExpression.getFieldType().equals(Date.class)) {
					return criteriaBuilder.notEqual(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
				} else if (!isValueExpression && fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.notEqual(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else {
					return criteriaBuilder.notEqual(fieldExpression.getExpression(), value);
				}
			case GREATER_THAN:
				if (!isValueExpression && fieldExpression.getFieldType().equals(Date.class)) {
					return criteriaBuilder.greaterThan(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
				} else if (!isValueExpression && fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.greaterThan(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else if (isValueExpression) {
					return criteriaBuilder.greaterThan(fieldExpression.getExpression(), (Expression) value);
				} else {
					return criteriaBuilder.greaterThan(fieldExpression.getExpression(), value.toString());
				}
			case GREATER_THAN_OR_EQUAL_TO:
				if (!isValueExpression && fieldExpression.getFieldType().equals(Date.class)) {
					return criteriaBuilder.greaterThanOrEqualTo(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
				} else if (!isValueExpression && fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.greaterThanOrEqualTo(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else if (isValueExpression) {
					return criteriaBuilder.greaterThanOrEqualTo(fieldExpression.getExpression(), (Expression) value);
				} else {
					return criteriaBuilder.greaterThanOrEqualTo(fieldExpression.getExpression(), value.toString());
				}
			case LESS_THAN:
				if (!isValueExpression && fieldExpression.getFieldType().equals(Date.class)) {
					return criteriaBuilder.lessThan(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
				} else if (!isValueExpression && fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.lessThan(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else if (isValueExpression) {
					return criteriaBuilder.lessThan(fieldExpression.getExpression(), (Expression) value);
				} else {
					return criteriaBuilder.lessThan(fieldExpression.getExpression(), value.toString());
				}
			case LESS_THAN_OR_EQUAL_TO:
				if (!isValueExpression && fieldExpression.getFieldType().equals(Date.class)) {
					return criteriaBuilder.lessThanOrEqualTo(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
				} else if (!isValueExpression && fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.lessThanOrEqualTo(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else if (isValueExpression) {
					return criteriaBuilder.lessThanOrEqualTo(fieldExpression.getExpression(), (Expression) value);
				} else {
					return criteriaBuilder.lessThanOrEqualTo(fieldExpression.getExpression(), value.toString());
				}
			case LIKE:
				if (isValueExpression) {
					return criteriaBuilder.like(fieldExpression.getExpression(), (Expression) value);
				} else {
					return criteriaBuilder.like(fieldExpression.getExpression(), "%" + value.toString() + "%");
				}
			case LIKE_START:
				if (isValueExpression) {
					return criteriaBuilder.like(fieldExpression.getExpression(), (Expression) value);
				} else {
					return criteriaBuilder.like(fieldExpression.getExpression(), value.toString() + "%");
				}
			case LIKE_END:
				if (isValueExpression) {
					return criteriaBuilder.like(fieldExpression.getExpression(), (Expression) value);
				} else {
					return criteriaBuilder.like(fieldExpression.getExpression(), "%" + value.toString());
				}
			case CUSTOM_LIKE:
				if (isValueExpression) {
					return criteriaBuilder.like(fieldExpression.getExpression(), (Expression) value);
				} else {
					return criteriaBuilder.like(fieldExpression.getExpression(), value.toString());
				}
			case BETWEEN:
				List<Object> values = (List<Object>) value;

				if (fieldExpression.getFieldType().equals(Date.class)) {
					return criteriaBuilder.between(
							fieldExpression.getExpression(),
							DateUtils.parseDate(values.get(0).toString()),
							DateUtils.parseDate(values.get(1).toString())
					);
				} else if (fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.between(
							fieldExpression.getExpression(),
							UUID.fromString(values.get(0).toString()),
							UUID.fromString(values.get(1).toString())
					);
				} else {
					return criteriaBuilder.between(fieldExpression.getExpression(), values.get(0).toString(), values.get(1).toString());
				}
			case IN:
				List<Object> newList = new ArrayList<Object>();
				List<Object> valueList = Arrays.asList(value.toString().split(","));

				for (Object v : valueList) {
					Object newValue = v.toString().trim();

					if (fieldExpression.getFieldType().equals(Date.class)) {
						newValue = DateUtils.parseDate(newValue.toString());
					} else if (fieldExpression.getFieldType().equals(UUID.class)) {
						newValue = UUID.fromString(newValue.toString());
					}

					newList.add(newValue);
				}

				return fieldExpression.getExpression().in(newList);
			case IS_NULL:
				return criteriaBuilder.isNull(fieldExpression.getExpression());
			case IS_NOT_NULL:
				return criteriaBuilder.isNotNull(fieldExpression.getExpression());
			default:
				break;
		}

		return null;
	}

	private Predicate joinPredicates(List<Predicate> predicates, LogicalOperator logicalOperator, CriteriaBuilder criteriaBuilder) {
		Predicate[] predicateArray = predicates.toArray(new Predicate[0]);

		switch (logicalOperator) {
			case AND:
				return criteriaBuilder.and(predicateArray);
			case OR:
				return criteriaBuilder.or(predicateArray);
			default:
				return null;
		}
	}

	@SuppressWarnings("unchecked")
	private FieldExpression getFieldExpressionByCondition(
		Condition condition, Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder
	) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, ParseException, InvocationTargetException {
		FieldExpression fieldExpression = this.getFieldExpressionByField(condition.getField(), condition.getJoinType(), condition.getFieldJoins(), root);

		if (Arrays.asList(new ConditionalOperator[] {
				ConditionalOperator.LIKE, ConditionalOperator.LIKE_START, 
				ConditionalOperator.LIKE_END, ConditionalOperator.CUSTOM_LIKE
		}).contains(condition.getConditionalOperator())) {
			fieldExpression.setExpression(fieldExpression.getExpression().as(String.class));
		}

		return fieldExpression;
	}

	@SuppressWarnings("rawtypes")
	private FieldExpression getFieldExpressionByField(String field, JoinType joinType, List<FieldJoin> fieldJoins, Root<T> root) throws NoSuchFieldException {
		Join<?, ?> join = null;
		Class<?> fieldType = null;
		String fieldName = null;
		String lastFieldName = null;
		List<Field> fields = ReflectionUtils.getFields(field, clazz);

		if (fields.size() > 1) {
			int j = 0;
			for (Field f : fields) {
				FieldJoin fieldJoin = null;
				String joinAlias = null;
				JoinType tempJoinType = copyJoinType(joinType);

				if (!fieldJoins.isEmpty()) {
					for (FieldJoin fj : fieldJoins) {
						if (f.getName().equals(fj.getField()) && (
								j == 0
										|| fj.getSourceField() == null
										|| fields.get(j - 1).getName().equals(fj.getSourceField())
						)
						) {
							fieldJoin = fj;
							break;
						}
					}
				}

				if (fieldJoin != null) {
					tempJoinType = fieldJoin.getType();
					joinAlias = fieldJoin.getAlias();
				}

				if (j == 0) {
					join = getJoin(root, lastFieldName, f.getName(), tempJoinType, joinAlias);
				} else if (j < fields.size() - 1) {
					join = getJoin(join, lastFieldName, f.getName(), tempJoinType, joinAlias);
				} else {
					fieldType = f.getType();
					fieldName = f.getName();
				}
				
				lastFieldName = f.getName();
				j++;
			}
		} else {
			fieldType = fields.get(0).getType();
			fieldName = fields.get(0).getName();
		}

		Expression expression = join != null ? join.get(fieldName) : root.get(fieldName);

		return new FieldExpression(expression, fieldType, fieldName);
	}
	
	private JoinType copyJoinType(JoinType joinType) {
		if (joinType == null) {
			return JoinType.INNER;
		}
		
		switch (joinType) {
			case INNER:
				return JoinType.INNER;
			case LEFT:
				return JoinType.LEFT;
			case RIGHT:
				return JoinType.RIGHT;
			default:
				return JoinType.INNER;	
		}
	}

	private Join<?, ?> getJoin(From<?, ?> from, String sourceField, String field, JoinType joinType, String alias) {		
		Optional<FieldJoin> optional = this.joins.stream()
			.filter(fj -> fj.getJoin() != null && (
				(sourceField != null && sourceField.equals(fj.getSourceField()))
				|| (sourceField == null && fj.getSourceField() == null)
			) && (
				(alias != null && alias.equals(fj.getAlias()))
				|| (alias == null && fj.getAlias() == null)
			) && field.equals(fj.getField()) && joinType.equals(fj.getType()))
			.findFirst();
		
		if (optional.isPresent()) {
			return optional.get().getJoin();
		} else {
			Join<?, ?> join = from.join(field, joinType);
			this.joins.add(new FieldJoin(sourceField, field, joinType, alias, join));
			
			return join;
		}
	}

	private CriteriaQuery<?> setGroupBy(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) throws NoSuchFieldException {
		if (!this.condition.getGroupBy().isEmpty()) {
			List<Expression<?>> expressions = new ArrayList<>();

			for (String groupBy : condition.getGroupBy()) {
				expressions.add(this.getFieldExpressionByField(groupBy, JoinType.INNER, condition.getFieldJoins(), root).getExpression());
			}

			query.groupBy(expressions);
		}

		return query;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private CriteriaQuery<?> setOrderBy(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) throws NoSuchFieldException,
			InvocationTargetException, NoSuchMethodException, ParseException, IllegalAccessException {
		if (!this.condition.getOrderBies().isEmpty()) {
			List<Order> orders = new ArrayList<>();

			for (OrderBy orderBy : this.condition.getOrderBies()) {
				if (orderBy.getCondition() != null) {
					List<Expression<?>> expressions = new ArrayList<>();

					if (orderBy.getCondition().getField() != null && !orderBy.getCondition().getField().isEmpty()) {
						expressions.add(this.getFieldExpressionByCondition(orderBy.getCondition(), root, query, criteriaBuilder).getExpression());
					}

					for (Case cas : orderBy.getCondition().getCases()) {
						CriteriaBuilder.Case cbCase = criteriaBuilder.selectCase();

						for (WhenThen whenThen : cas.getWhenThens()) {
							if (whenThen.getWhen() != null) {
								Predicate whenPredicate = this.addCondition(whenThen.getWhen(), LogicalOperator.AND, new ArrayList<Predicate>(), true, root, query, criteriaBuilder).get(0);

								if (whenThen.getExpressionThen() != null && !whenThen.getExpressionThen().isEmpty()) {
									cbCase.when(whenPredicate, this.getFieldExpressionByField(whenThen.getExpressionThen(), JoinType.INNER, condition.getFieldJoins(), root).getExpression());
								} else {
									cbCase.when(whenPredicate, whenThen.getRawThen());
								}
							}
						}

						if (cas.getExpressionOtherwise() != null && !cas.getExpressionOtherwise().isEmpty()) {
							cbCase.otherwise(this.getFieldExpressionByField(cas.getExpressionOtherwise(), JoinType.INNER, condition.getFieldJoins(), root).getExpression());
						} else {
							cbCase.otherwise(cas.getRawOtherwise());
						}

						expressions.add(cbCase);
					}

					for (Expression<?> expression : expressions) {
						Order order = null;
						if (orderBy.getDirection().equals(OrderDirection.ASC)) {
							order = criteriaBuilder.asc(expression);
						} else if (orderBy.getDirection().equals(OrderDirection.ASC)) {
							order = criteriaBuilder.desc(expression);
						}

						orders.add(order);
					}
				}
			}

			query.orderBy(orders);
		}

		return query;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	private void resetJoins() {
		this.joins = new ArrayList<>();
	}

}

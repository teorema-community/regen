package br.inf.teorema.regen.specification;

import br.inf.teorema.regen.enums.ConditionalOperator;
import br.inf.teorema.regen.enums.FunctionType;
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
import java.util.stream.Collectors;

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
			query.distinct(condition.getDistinct());	
			
			query = setGroupBy(root, query, criteriaBuilder);
			query = setOrderBy(root, query, criteriaBuilder);
			query = setHaving(root, query, criteriaBuilder);

			return addCondition(
				this.condition, LogicalOperator.AND, new ArrayList<Predicate>(), true, root, query, criteriaBuilder, this.clazz
			).get(0);
		} catch (NoSuchFieldException | ParseException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
			throw new NullPointerException(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	private List<Predicate> addCondition(
		Condition condition, LogicalOperator logicalOperator, List<Predicate> predicates, boolean last, From<?, ?> from, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, Class<?> clazz
	) throws NoSuchFieldException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		if (!condition.getConditions().isEmpty()) {
			List<Predicate> tempPredicates = new ArrayList<>();

			int i = 0;
			for (Condition subCondition : condition.getConditions()) {
				tempPredicates = addCondition(
					subCondition, condition.getLogicalOperator(), tempPredicates, i >= condition.getConditions().size() - 1, from, query, criteriaBuilder, clazz
				);
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
			FieldExpression fieldExpression = getFieldExpressionByCondition(condition, from, query, criteriaBuilder, clazz);
			Object value = condition.getValue();
			boolean isValueExpression = false;

			if (condition.getExpressionValue() != null) {
				isValueExpression = true;
				value = this.getFieldExpressionByField(
					condition.getExpressionValue(), condition.getJoinType(), condition.getFieldJoins(), from, query, criteriaBuilder, clazz
				).getExpression();
			}

			Predicate predicate = createPredicate(
				fieldExpression, condition.getConditionalOperator(), value, isValueExpression, criteriaBuilder
			);
			
			if (condition.getNot() != null && condition.getNot()) {
				predicate = criteriaBuilder.not(predicate);
			}
			
			predicates.add(predicate);
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
	) throws ParseException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {		
		if (!isValueExpression && !Arrays.asList(new ConditionalOperator[] {
			ConditionalOperator.BETWEEN, ConditionalOperator.IN
		}).contains(conditionalOperator)) {
			value = ReflectionUtils.convertValueToEnumIfNeeded(fieldExpression.getFieldType(), value);
			
			if (fieldExpression.getFieldType().equals(Date.class)) {
				value = DateUtils.getDateValue(value);
			}
		}
		
		switch (conditionalOperator) {
			case EQUALS:				
				if (!isValueExpression && value instanceof Date) {
					return criteriaBuilder.equal(fieldExpression.getExpression(), DateUtils.getDateValue(value));
				} else if (!isValueExpression && fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.equal(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else if (isValueExpression) {
					return criteriaBuilder.equal(fieldExpression.getExpression(), (Expression) value);
				} else {
					return criteriaBuilder.equal(fieldExpression.getExpression(), value);
				}
			case NOT_EQUALS:
				if (!isValueExpression && value instanceof Date) {
					return criteriaBuilder.notEqual(fieldExpression.getExpression(), DateUtils.getDateValue(value));
				} else if (!isValueExpression && fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.notEqual(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else if (isValueExpression) {
					return criteriaBuilder.notEqual(fieldExpression.getExpression(), (Expression) value);
				} else {
					return criteriaBuilder.notEqual(fieldExpression.getExpression(), value);
				}
			case GREATER_THAN:				
				if (!isValueExpression && value instanceof Date) {
					return criteriaBuilder.greaterThan(fieldExpression.getExpression(), (Date) value);
				} else if (!isValueExpression && fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.greaterThan(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else if (isValueExpression) {
					return criteriaBuilder.greaterThan(fieldExpression.getExpression(), (Expression) value);
				} else {
					return criteriaBuilder.greaterThan(fieldExpression.getExpression(), value.toString());
				}
			case GREATER_THAN_OR_EQUAL_TO:
				if (!isValueExpression && value instanceof Date) {
					return criteriaBuilder.greaterThanOrEqualTo(fieldExpression.getExpression(), (Date) value);
				} else if (!isValueExpression && fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.greaterThanOrEqualTo(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else if (isValueExpression) {
					return criteriaBuilder.greaterThanOrEqualTo(fieldExpression.getExpression(), (Expression) value);
				} else {
					return criteriaBuilder.greaterThanOrEqualTo(fieldExpression.getExpression(), value.toString());
				}
			case LESS_THAN:
				if (!isValueExpression && value instanceof Date) {
					return criteriaBuilder.lessThan(fieldExpression.getExpression(), (Date) value);
				} else if (!isValueExpression && fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.lessThan(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else if (isValueExpression) {
					return criteriaBuilder.lessThan(fieldExpression.getExpression(), (Expression) value);
				} else {
					return criteriaBuilder.lessThan(fieldExpression.getExpression(), value.toString());
				}
			case LESS_THAN_OR_EQUAL_TO:
				if (!isValueExpression && value instanceof Date) {
					return criteriaBuilder.lessThanOrEqualTo(fieldExpression.getExpression(), (Date) value);
				} else if (!isValueExpression && fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.lessThanOrEqualTo(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else if (isValueExpression) {
					return criteriaBuilder.lessThanOrEqualTo(fieldExpression.getExpression(), (Expression) value);
				} else {
					return criteriaBuilder.lessThanOrEqualTo(fieldExpression.getExpression(), value.toString());
				}
			case LIKE:
			case LIKE_START:
			case LIKE_END:
			case CUSTOM_LIKE:
				if (isValueExpression) {
					return criteriaBuilder.like(fieldExpression.getExpression(), (Expression) value);
				} else {
					value = value.toString();
					
					if (conditionalOperator.equals(ConditionalOperator.LIKE) || conditionalOperator.equals(ConditionalOperator.LIKE_END)) {
						value = "%" + value;
					}
					
					if (conditionalOperator.equals(ConditionalOperator.LIKE) || conditionalOperator.equals(ConditionalOperator.LIKE_START)) {
						value = value + "%";
					}
					
					return criteriaBuilder.like(fieldExpression.getExpression(), (String) value);
				}
			case BETWEEN:
				return createBetweenPredicate(fieldExpression, value, criteriaBuilder);
			case IN:
				return fieldExpression.getExpression().in(convertValueToList(fieldExpression, value, false));
			case IS_NULL:
				return criteriaBuilder.isNull(fieldExpression.getExpression());
			case IS_NOT_NULL:
				return criteriaBuilder.isNotNull(fieldExpression.getExpression());
			default:
				break;
		}

		return null;
	}
	
	@SuppressWarnings("unchecked")
	private Predicate createBetweenPredicate(FieldExpression fieldExpression, Object value, CriteriaBuilder criteriaBuilder) 
		throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		List<Object> values = convertValueToList(fieldExpression, value, true);
		
		if (values.get(0) instanceof Date) {
            return criteriaBuilder.between(
                            fieldExpression.getExpression(),
                            (Date) values.get(0),
                            (Date) values.get(1)
            );
	    } else if (fieldExpression.getFieldType().equals(UUID.class)) {
            return criteriaBuilder.between(
                            fieldExpression.getExpression(),
                            (UUID) values.get(0),
                            (UUID) values.get(1)
            );
	    } else {
            return criteriaBuilder.between(fieldExpression.getExpression(), values.get(0).toString(), values.get(1).toString());
	    }
	}
	
	@SuppressWarnings("unchecked")
	private List<Object> convertValueToList(FieldExpression fieldExpression, Object value, boolean convertToStringOnElse) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		List<Object> newList = new ArrayList<Object>();
		List<Object> valueList = null;
		if (value instanceof String) {
			valueList = Arrays.asList(value.toString().split(",")).stream().map(v -> v.trim()).collect(Collectors.toList());
		} else {
			valueList = (List<Object>) value;
		}

		for (Object v : valueList) {
			Object newValue = v;
			newValue = ReflectionUtils.convertValueToEnumIfNeeded(fieldExpression.getFieldType(), newValue);

			if (fieldExpression.getFieldType().equals(Date.class)) {
				newValue = DateUtils.getDateValue(newValue);
			} else if (fieldExpression.getFieldType().equals(UUID.class)) {
				newValue = UUID.fromString(newValue.toString());
			} else if (convertToStringOnElse) {
				newValue = newValue.toString();
			}

			newList.add(newValue);
		}
		
		return newList;
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
		Condition condition, From<?, ?> from, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, Class<?> clazz
	) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, ParseException, InvocationTargetException {
		System.out.println();
		System.out.println("Entrou no método getFieldExpressionByCondition");
		System.out.println("condition: " + condition);
		System.out.println("from: " + from);
		System.out.println("condition.getField(): " + condition.getField());
		System.out.println("condition.getJoinType(): " + condition.getJoinType());
		System.out.println("condition.getFieldJoins(): " + condition.getFieldJoins());
		
		FieldExpression fieldExpression = this.getFieldExpressionByField(
			condition.getField(), condition.getJoinType(), condition.getFieldJoins(), from, query, criteriaBuilder, clazz
		);
		
		System.out.println("fieldExpression: " + fieldExpression);

		if (Arrays.asList(new ConditionalOperator[] {
				ConditionalOperator.LIKE, ConditionalOperator.LIKE_START, 
				ConditionalOperator.LIKE_END, ConditionalOperator.CUSTOM_LIKE
		}).contains(condition.getConditionalOperator())) {
			fieldExpression.setExpression(fieldExpression.getExpression().as(String.class));
		}

		return fieldExpression;
	}

	@SuppressWarnings("rawtypes")
	public FieldExpression getFieldExpressionByField(
		String field, JoinType joinType, List<FieldJoin> fieldJoins, From<?, ?> from, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, Class<?> clazz
	) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException {
		System.out.println("");
		System.out.println("Entrou em getFieldExpressionByField");
		System.out.println("field: " + field);
		System.out.println("joinType: " + joinType);
		System.out.println("fieldJoins: " + fieldJoins);
		System.out.println("from: " + from);
		
		Join<?, ?> join = null;
		Class<?> fieldType = null;
		String fieldName = null;
		String lastFieldName = null;
		Function function = Function.extractFunctionFromfield(field, this, joinType, fieldJoins, from, query, criteriaBuilder, clazz);
		
		System.out.println("function: " + function);
		
		if (function != null) {
			return new FieldExpression(function);
		} else {
			List<Field> fields = ReflectionUtils.getFields(field, clazz);
			System.out.println("fields: " + fields);
			System.out.println("fields.size(): " + fields.size());
	
			if (fields.size() > 1) {
				int j = 0;
				for (Field f : fields) {
					System.out.println("f: " + f);
					System.out.println("f.getName(): " + f.getName());
					
					FieldJoin fieldJoin = null;
					String joinAlias = null;
					JoinType tempJoinType = copyJoinType(joinType);
					System.out.println("tempJoinType: " + tempJoinType);
					Condition on = null;
	
					if (!fieldJoins.isEmpty()) {
						System.out.println("Entrou no if 1");
						
						for (FieldJoin fj : fieldJoins) {
							System.out.println("fj: " + fj);
							System.out.println("fj.getField(): " + fj.getField());
							System.out.println("j: " + j);
							System.out.println("fj.getSourceField(): " + fj.getSourceField());
							
							if (f.getName().equals(fj.getField()) && (
									j == 0
											|| fj.getSourceField() == null
											|| fields.get(j - 1).getName().equals(fj.getSourceField())
							)
							) {
								System.out.println("Entrou no if 2");
								fieldJoin = fj;
								break;
							}
						}
					}
					
					System.out.println("fieldJoin: " + fieldJoin);
	
					if (fieldJoin != null) {
						System.out.println("Entrou no if 3");
						tempJoinType = fieldJoin.getType();
						joinAlias = fieldJoin.getAlias();
						on = fieldJoin.getOn();
					}
					
					System.out.println("tempJoinType: " + tempJoinType);
					System.out.println("joinAlias: " + joinAlias);
					System.out.println("on: " + on);
					System.out.println("j: " + j);
					fieldType = ReflectionUtils.getFieldEntityOrType(f);
	
					if (j == 0) {
						join = getJoin(from, lastFieldName, f.getName(), tempJoinType, joinAlias, on, query, criteriaBuilder, fieldType);
					} else if (j < fields.size() - 1) {
						join = getJoin(join, lastFieldName, f.getName(), tempJoinType, joinAlias, on, query, criteriaBuilder, fieldType);
					} else {
						fieldName = f.getName();
					}
					
					System.out.println("fieldType: " + fieldType);
					System.out.println("fieldName: " + fieldName);
					
					lastFieldName = f.getName();
					System.out.println("lastFieldName: " + lastFieldName);
					j++;
				}
			} else {
				fieldType = fields.get(0).getType();
				fieldName = fields.get(0).getName();
			}
			
			System.out.println("join: " + join);
			System.out.println("fieldName: " + fieldName);
	
			Expression expression = join != null ? join.get(fieldName) : from.get(fieldName);
			System.out.println("expression: " + expression);
	
			return new FieldExpression(expression, fieldType, fieldName);
		}
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

	private Join<?, ?> getJoin(
		From<?, ?> from, String sourceField, String field, JoinType joinType, String alias, Condition on, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, Class<?> clazz
	) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException {
		System.out.println("Entrou em getJoin");
		System.out.println("from: " + from);
		System.out.println("sourceField: " + sourceField);
		System.out.println("field: " + field);
		System.out.println("joinType: " + joinType);
		System.out.println("alias: " + alias);
		System.out.println("on: " + on);
		System.out.println("this.joins: " + this.joins);
		
		Optional<FieldJoin> optional = this.joins.stream()
			.filter(fj -> fj.getJoin() != null && (
				(sourceField != null && sourceField.equals(fj.getSourceField()))
				|| (sourceField == null && fj.getSourceField() == null)
			) && (
				(alias != null && alias.equals(fj.getAlias()))
				|| (alias == null && fj.getAlias() == null)
			) && field.equals(fj.getField()) && joinType.equals(fj.getType()))
			.findFirst();
		
		System.out.println("optional.isPresent(): " + optional.isPresent());
		
		if (optional.isPresent()) {
			System.out.println("optional.get(): " + optional.get());
			System.out.println("optional.get().getJoin(): " + optional.get().getJoin());
			return optional.get().getJoin();
		} else {
			Join<?, ?> join = from.join(field, joinType);
			System.out.println("join: " + join);
			
			if (on != null) {
				List<Predicate> predicates = addCondition(
					on, LogicalOperator.AND, new ArrayList<Predicate>(), true, join, query, criteriaBuilder, clazz
				);
				
				System.out.println("predicates: " + predicates);
				
				if (predicates != null && !predicates.isEmpty()) {
					join.on(predicates.toArray(new Predicate[predicates.size()]));
				}
			}
			
			FieldJoin newFieldJoin = new FieldJoin(sourceField, field, joinType, alias, on, join);
			System.out.println("newFieldJoin: " + newFieldJoin);
			this.joins.add(newFieldJoin);
			System.out.println("this.joins: " + this.joins);
			
			return join;
		}
	}

	private CriteriaQuery<?> setGroupBy(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException {
		if (!this.condition.getGroupBy().isEmpty()) {
			List<Expression<?>> expressions = new ArrayList<>();

			for (String groupBy : condition.getGroupBy()) {
				expressions.add(this.getFieldExpressionByField(
					groupBy, JoinType.INNER, condition.getFieldJoins(), root, query, criteriaBuilder, this.clazz
				).getExpression());
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
						expressions.add(this.getFieldExpressionByCondition(
							orderBy.getCondition(), root, query, criteriaBuilder, this.clazz
						).getExpression());
					}

					for (Case cas : orderBy.getCondition().getCases()) {
						CriteriaBuilder.Case cbCase = criteriaBuilder.selectCase();

						for (WhenThen whenThen : cas.getWhenThens()) {
							if (whenThen.getWhen() != null) {
								Predicate whenPredicate = this.addCondition(
									whenThen.getWhen(), LogicalOperator.AND, new ArrayList<Predicate>(), true, root, query, criteriaBuilder, this.clazz
								).get(0);

								if (whenThen.getExpressionThen() != null && !whenThen.getExpressionThen().isEmpty()) {
									cbCase.when(whenPredicate, this.getFieldExpressionByField(
										whenThen.getExpressionThen(), JoinType.INNER, condition.getFieldJoins(), root, query, criteriaBuilder, this.clazz
									).getExpression());
								} else {
									cbCase.when(whenPredicate, whenThen.getRawThen());
								}
							}
						}

						if (cas.getExpressionOtherwise() != null && !cas.getExpressionOtherwise().isEmpty()) {
							cbCase.otherwise(this.getFieldExpressionByField(
								cas.getExpressionOtherwise(), JoinType.INNER, condition.getFieldJoins(), root, query, criteriaBuilder, this.clazz
							).getExpression());
						} else {
							cbCase.otherwise(cas.getRawOtherwise());
						}

						expressions.add(cbCase);
					}

					for (Expression<?> expression : expressions) {
						Order order = null;
						if (orderBy.getDirection().equals(OrderDirection.ASC)) {
							order = criteriaBuilder.asc(expression);
						} else if (orderBy.getDirection().equals(OrderDirection.DESC)) {
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
	
	private CriteriaQuery<?> setHaving(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, ParseException {
		if (condition.getHaving() != null) {
			//FieldExpression fieldExpression = getFieldExpressionByCondition(condition.getHaving(), root, query, criteriaBuilder);
			List<Predicate> havingPredicates = addCondition(
				condition.getHaving(), LogicalOperator.AND, new ArrayList<Predicate>(), true, root, query, criteriaBuilder, this.clazz
			);
			query.having(havingPredicates.toArray(new Predicate[havingPredicates.size()]));
			/*query.having(criteriaBuilder.lessThanOrEqualTo(				
				criteriaBuilder.sum(criteriaBuilder.diff(root.join("balances").get("entries"), root.join("balances").get("exits"))),
				0				
			));*/
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

package br.inf.teorema.regen.specification;

import br.inf.teorema.regen.constants.ConditionalOperator;
import br.inf.teorema.regen.constants.LogicalOperator;
import br.inf.teorema.regen.model.Condition;
import br.inf.teorema.regen.model.FieldExpression;
import br.inf.teorema.regen.model.FieldJoin;
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

	public GenericSpecification(Condition condition, Class<?> clazz) {
		super();
		this.condition = condition;
		this.clazz = clazz;
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		try {
			query = setGroupBy(root, query, criteriaBuilder);
			return addCondition(this.condition, null, new ArrayList<Predicate>(), true, root, query, criteriaBuilder).get(0);
		} catch (NoSuchFieldException | ParseException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
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
					|| condition.getConditionalOperator().equals(ConditionalOperator.IS_NOT_NULL)
					|| condition.getConditionalOperator().equals(ConditionalOperator.IS_NULL)
				)
		) {
			FieldExpression fieldExpression = getFieldExpressionByCondition(condition, root, query, criteriaBuilder);
			Object value = condition.getValue();

			if (fieldExpression.getFieldType().isEnum() && value != null && !(value instanceof Enum<?>)) {
				value = fieldExpression.getFieldType().getDeclaredMethod("valueOf", String.class).invoke(null, value.toString());
			}

			predicates.add(createPredicate(
				fieldExpression, condition.getConditionalOperator(), value, criteriaBuilder
			));
		}

		if (last && logicalOperator != null) {
			Predicate predicate = joinPredicates(predicates, logicalOperator, criteriaBuilder);
			predicates = new ArrayList<>();
			predicates.add(predicate);
		}

		return predicates;
	}

	private Predicate createPredicate(
		FieldExpression fieldExpression, ConditionalOperator conditionalOperator, Object value, CriteriaBuilder criteriaBuilder
	) throws ParseException {
		switch (conditionalOperator) {
			case EQUALS:
				if (fieldExpression.getFieldType().equals(Date.class)) {
					return criteriaBuilder.equal(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
				} else if (fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.equal(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else {
					return criteriaBuilder.equal(fieldExpression.getExpression(), value);
				}
			case NOT_EQUALS:
				if (fieldExpression.getFieldType().equals(Date.class)) {
					return criteriaBuilder.notEqual(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
				} else if (fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.notEqual(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else {
					return criteriaBuilder.notEqual(fieldExpression.getExpression(), value);
				}
			case GREATER_THAN:
				if (fieldExpression.getFieldType().equals(Date.class)) {
					return criteriaBuilder.greaterThan(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
				} else if (fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.greaterThan(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else {
					return criteriaBuilder.greaterThan(fieldExpression.getExpression(), value.toString());
				}
			case GREATER_THAN_OR_EQUAL_TO:
				if (fieldExpression.getFieldType().equals(Date.class)) {
					return criteriaBuilder.greaterThanOrEqualTo(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
				} else if (fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.greaterThanOrEqualTo(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else {
					return criteriaBuilder.greaterThanOrEqualTo(fieldExpression.getExpression(), value.toString());
				}
			case LESS_THAN:
				if (fieldExpression.getFieldType().equals(Date.class)) {
					return criteriaBuilder.lessThan(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
				} else if (fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.lessThan(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else {
					return criteriaBuilder.lessThan(fieldExpression.getExpression(), value.toString());
				}
			case LESS_THAN_OR_EQUAL_TO:
				if (fieldExpression.getFieldType().equals(Date.class)) {
					return criteriaBuilder.lessThanOrEqualTo(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
				} else if (fieldExpression.getFieldType().equals(UUID.class)) {
					return criteriaBuilder.lessThanOrEqualTo(fieldExpression.getExpression(), UUID.fromString(value.toString()));
				} else {
					return criteriaBuilder.lessThanOrEqualTo(fieldExpression.getExpression(), value.toString());
				}
			case LIKE:
				return criteriaBuilder.like(fieldExpression.getExpression(), "%" + value.toString() + "%");
			case LIKE_START:
				return criteriaBuilder.like(fieldExpression.getExpression(), value.toString() + "%");
			case LIKE_END:
				return criteriaBuilder.like(fieldExpression.getExpression(), "%" + value.toString());
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

	private FieldExpression getFieldExpressionByCondition(
		Condition condition, Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder
	) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, ParseException, InvocationTargetException {
		Join<?, ?> join = null;
		Class<?> fieldType = null;
		String fieldName = null;
		List<Field> fields = ReflectionUtils.getFields(condition.getField(), clazz);

		if (fields.size() > 1) {
			int j = 0;
			for (Field f : fields) {
				JoinType joinType = condition.getJoinType();
				FieldJoin fieldJoin = null;

				if (!condition.getFieldJoins().isEmpty()) {
					for (FieldJoin fj : condition.getFieldJoins()) {
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
					joinType = fieldJoin.getType();
				}

				if (j == 0) {
					join = root.join(f.getName(), joinType);
				} else if (j < fields.size() - 1) {
					join = join.join(f.getName(), joinType);
				} else {
					fieldType = f.getType();
					fieldName = f.getName();
				}

				j++;
			}
		} else {
			fieldType = fields.get(0).getType();
			fieldName = fields.get(0).getName();
		}

		Expression expression = join != null ? join.get(fieldName) : root.get(fieldName);

		if (Arrays.asList(new ConditionalOperator[] {
				ConditionalOperator.LIKE, ConditionalOperator.LIKE_START, ConditionalOperator.LIKE_END
		}).contains(condition.getConditionalOperator())) {
			expression = expression.as(String.class);
		}

		return new FieldExpression(expression, fieldType, fieldName);
	}

	private CriteriaQuery<?> setGroupBy(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		if (!this.condition.getGroupBy().isEmpty()) {
			List<Expression<?>> expressions = new ArrayList<>();

			for (String groupBy : condition.getGroupBy()) {
				expressions.add(root.get(groupBy));
			}

			query.groupBy(expressions);
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

}

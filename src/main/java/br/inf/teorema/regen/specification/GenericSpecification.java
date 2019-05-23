package br.inf.teorema.regen.specification;

import br.inf.teorema.regen.constants.ConditionalOperator;
import br.inf.teorema.regen.constants.LogicalOperator;
import br.inf.teorema.regen.model.FieldExpression;
import br.inf.teorema.regen.model.FieldJoin;
import org.springframework.data.jpa.domain.Specification;

import br.inf.teorema.regen.model.Condition;
import br.inf.teorema.regen.util.DateUtils;
import br.inf.teorema.regen.util.ReflectionUtils;

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
					|| (
						condition.getFieldValue() != null
						&& !condition.getFieldValue().toString().isEmpty()
					)
					|| condition.getConditionalOperator().equals(ConditionalOperator.IS_NOT_NULL)
					|| condition.getConditionalOperator().equals(ConditionalOperator.IS_NULL)
				)
		) {
			FieldExpression fieldExpression = getFieldExpressionByCondition(condition, root, query, criteriaBuilder);
			Object value = condition.getValue();

			if (!fieldExpression.getFieldType().isEnum() && value != null && !(value instanceof Enum<?>)) {
				value = fieldExpression.getFieldType().getDeclaredMethod("valueOf", String.class).invoke(null, value.toString());
			}

			Predicate predicate = null;

			switch (condition.getConditionalOperator()) {
				case EQUALS:
					if (condition.getFieldValue() == null && fieldExpression.getFieldType().equals(Date.class)) {
						predicate = criteriaBuilder.equal(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
					} else if (condition.getFieldValue() == null && fieldExpression.getFieldType().equals(UUID.class)) {
						predicate = criteriaBuilder.equal(fieldExpression.getExpression(), UUID.fromString(value.toString()));
					} else {
						predicate = criteriaBuilder.equal(fieldExpression.getExpression(), value);
					}

					break;
				case NOT_EQUALS:
					if (condition.getFieldValue() == null && fieldExpression.getFieldType().equals(Date.class)) {
						predicate = criteriaBuilder.notEqual(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
					} else if (condition.getFieldValue() == null && fieldExpression.getFieldType().equals(UUID.class)) {
						predicate = criteriaBuilder.notEqual(fieldExpression.getExpression(), UUID.fromString(value.toString()));
					} else {
						predicate = criteriaBuilder.notEqual(fieldExpression.getExpression(), value);
					}

					break;
				case GREATER_THAN:
					if (condition.getFieldValue() == null && fieldExpression.getFieldType().equals(Date.class)) {
						predicate = criteriaBuilder.greaterThan(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
					} else if (condition.getFieldValue() == null && fieldExpression.getFieldType().equals(UUID.class)) {
						predicate = criteriaBuilder.greaterThan(fieldExpression.getExpression(), UUID.fromString(value.toString()));
					} else {
						predicate = criteriaBuilder.greaterThan(fieldExpression.getExpression(), value.toString());
					}

					break;
				case GREATER_THAN_OR_EQUAL_TO:
					if (condition.getFieldValue() == null && fieldExpression.getFieldType().equals(Date.class)) {
						predicate = criteriaBuilder.greaterThanOrEqualTo(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
					} else if (condition.getFieldValue() == null && fieldExpression.getFieldType().equals(UUID.class)) {
						predicate = criteriaBuilder.greaterThanOrEqualTo(fieldExpression.getExpression(), UUID.fromString(value.toString()));
					} else {
						predicate = criteriaBuilder.greaterThanOrEqualTo(fieldExpression.getExpression(), value.toString());
					}

					break;
				case LESS_THAN:
					if (condition.getFieldValue() == null && fieldExpression.getFieldType().equals(Date.class)) {
						predicate = criteriaBuilder.lessThan(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
					} else if (condition.getFieldValue() == null && fieldExpression.getFieldType().equals(UUID.class)) {
						predicate = criteriaBuilder.lessThan(fieldExpression.getExpression(), UUID.fromString(value.toString()));
					} else {
						predicate = criteriaBuilder.lessThan(fieldExpression.getExpression(), value.toString());
					}

					break;
				case LESS_THAN_OR_EQUAL_TO:
					if (condition.getFieldValue() == null && fieldExpression.getFieldType().equals(Date.class)) {
						predicate = criteriaBuilder.lessThanOrEqualTo(fieldExpression.getExpression(), DateUtils.parseDate(value.toString()));
					} else if (condition.getFieldValue() == null && fieldExpression.getFieldType().equals(UUID.class)) {
						predicate = criteriaBuilder.lessThanOrEqualTo(fieldExpression.getExpression(), UUID.fromString(value.toString()));
					} else {
						predicate = criteriaBuilder.lessThanOrEqualTo(fieldExpression.getExpression(), value.toString());
					}

					break;
				case LIKE:
					if (condition.getFieldValue() == null) {
						predicate = criteriaBuilder.like(fieldExpression.getExpression(), "%" + value.toString() + "%");
					} else {
						predicate = criteriaBuilder.like(fieldExpression.getExpression(), (Expression) value);
					}

					break;
				case LIKE_START:
					if (condition.getFieldValue() == null) {
						predicate = criteriaBuilder.like(fieldExpression.getExpression(), value.toString() + "%");
					} else {
						predicate = criteriaBuilder.like(fieldExpression.getExpression(), (Expression) value);
					}

					break;
				case LIKE_END:
					if (condition.getFieldValue() == null) {
						predicate = criteriaBuilder.like(fieldExpression.getExpression(), "%" + value.toString());
					} else {
						predicate = criteriaBuilder.like(fieldExpression.getExpression(), (Expression) value);
					}

					break;
				case BETWEEN:
					List<Object> values = (List<Object>) value;

					if (condition.getFieldValue() == null) {
						if (fieldExpression.getFieldType().equals(Date.class)) {
							predicate = criteriaBuilder.between(
									fieldExpression.getExpression(),
									DateUtils.parseDate(values.get(0).toString()),
									DateUtils.parseDate(values.get(1).toString())
							);
						} else if (fieldExpression.getFieldType().equals(UUID.class)) {
							predicate = criteriaBuilder.between(
									fieldExpression.getExpression(),
									UUID.fromString(values.get(0).toString()),
									UUID.fromString(values.get(1).toString())
							);
						} else {
							predicate = criteriaBuilder.between(fieldExpression.getExpression(), values.get(0).toString(), values.get(1).toString());
						}
					} else {
						predicate = criteriaBuilder.between(fieldExpression.getExpression(), (Expression) values.get(0), (Expression) values.get(1));
					}

					break;
				case IN:
					List<Object> newList = new ArrayList<Object>();

					if (condition.getFieldValue() == null) {
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
					} else {
						try {
							newList = (List) value;
						} catch (ClassCastException e) {
							newList.add(value);
						}
					}

					predicate = fieldExpression.getExpression().in(newList);

					break;
				case IS_NULL:
					predicate = criteriaBuilder.isNull(fieldExpression.getExpression());

					break;
				case IS_NOT_NULL:
					predicate = criteriaBuilder.isNotNull(fieldExpression.getExpression());

					break;
				default:
					break;
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

	private Join<?,?> setJoinCustomOn(Join<?,?> join, Condition on, Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, ParseException, NoSuchFieldException {
		if (on != null && on.getFieldValue() != null) {
			on.setValue(getFieldExpressionByCondition(on, root, query, criteriaBuilder).getExpression());

			List<Predicate> predicates = addCondition(on, null, new ArrayList<Predicate>(), true, root, query, criteriaBuilder);

			if (!predicates.isEmpty()) {
				join.on(predicates.toArray(new Predicate[predicates.size()]));
			}
		}

		return join;
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
						if (f.getName().equals(fieldJoin.getField()) && (
								j == 0
										|| fieldJoin.getSourceField() == null
										|| fields.get(j - 1).getName().equals(fieldJoin.getSourceField())
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
					break;
				}

				if (fieldJoin != null) {
					join = setJoinCustomOn(join, fieldJoin.getOn(), root, query, criteriaBuilder);
				}

				j++;
			}
		} else {
			fieldType = fields.get(0).getType();
			fieldName = fields.get(0).getName();
		}

		Expression expression = join != null ? join.get(fieldName) : root.get(fieldName);

		return new FieldExpression(expression, fieldType, fieldName);
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

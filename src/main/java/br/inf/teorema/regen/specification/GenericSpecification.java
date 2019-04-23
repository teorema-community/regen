package br.inf.teorema.regen.specification;

import br.inf.teorema.regen.constants.LogicalOperator;
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
		if (condition.getConditions() != null) {
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
				&& condition.getValue() != null
				&& !condition.getValue().toString().isEmpty()) {
			Object value = condition.getValue();
			Predicate predicate = null;
			Join<?, ?> join = null;
			Class<?> fieldType = null;
			String fieldName = null;
			List<Field> fields = ReflectionUtils.getFields(condition.getField(), clazz);

			if (fields.size() > 1) {
				int j = 0;
				for (Field f : fields) {
					if (j == 0) {
						join = root.join(f.getName(), condition.getJoinType());
					} else if (j < fields.size() - 1) {
						join = join.join(f.getName(), condition.getJoinType());
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

			if (fieldType.isEnum() && !(value instanceof Enum<?>)) {
				value = fieldType.getDeclaredMethod("valueOf", String.class).invoke(null, value.toString());
			}

			@SuppressWarnings("rawtypes")
			Expression expression = join != null ? join.get(fieldName) : root.get(fieldName);

			switch (condition.getConditionalOperator()) {
				case EQUALS:
					if (fieldType.equals(Date.class)) {
						predicate = criteriaBuilder.equal(expression, DateUtils.parseDate(value.toString()));
					} else if (fieldType.equals(UUID.class)) {
						predicate = criteriaBuilder.equal(expression, UUID.fromString(value.toString()));
					} else {
						predicate = criteriaBuilder.equal(expression, value);
					}

					break;
				case NOT_EQUALS:
					if (fieldType.equals(Date.class)) {
						predicate = criteriaBuilder.notEqual(expression, DateUtils.parseDate(value.toString()));
					} else if (fieldType.equals(UUID.class)) {
						predicate = criteriaBuilder.notEqual(expression, UUID.fromString(value.toString()));
					} else {
						predicate = criteriaBuilder.notEqual(expression, value);
					}

					break;
				case GREATER_THAN:
					if (fieldType.equals(Date.class)) {
						predicate = criteriaBuilder.greaterThan(expression, DateUtils.parseDate(value.toString()));
					} else if (fieldType.equals(UUID.class)) {
						predicate = criteriaBuilder.greaterThan(expression, UUID.fromString(value.toString()));
					} else {
						predicate = criteriaBuilder.greaterThan(expression, value.toString());
					}

					break;
				case GREATER_THAN_OR_EQUAL_TO:
					if (fieldType.equals(Date.class)) {
						predicate = criteriaBuilder.greaterThanOrEqualTo(expression, DateUtils.parseDate(value.toString()));
					} else if (fieldType.equals(UUID.class)) {
						predicate = criteriaBuilder.greaterThanOrEqualTo(expression, UUID.fromString(value.toString()));
					} else {
						predicate = criteriaBuilder.greaterThanOrEqualTo(expression, value.toString());
					}

					break;
				case LESS_THAN:
					if (fieldType.equals(Date.class)) {
						predicate = criteriaBuilder.lessThan(expression, DateUtils.parseDate(value.toString()));
					} else if (fieldType.equals(UUID.class)) {
						predicate = criteriaBuilder.lessThan(expression, UUID.fromString(value.toString()));
					} else {
						predicate = criteriaBuilder.lessThan(expression, value.toString());
					}

					break;
				case LESS_THAN_OR_EQUAL_TO:
					if (fieldType.equals(Date.class)) {
						predicate = criteriaBuilder.lessThanOrEqualTo(expression, DateUtils.parseDate(value.toString()));
					} else if (fieldType.equals(UUID.class)) {
						predicate = criteriaBuilder.lessThanOrEqualTo(expression, UUID.fromString(value.toString()));
					} else {
						predicate = criteriaBuilder.lessThanOrEqualTo(expression, value.toString());
					}

					break;
				case LIKE:
					predicate = criteriaBuilder.like(expression, "%" + value.toString() + "%");
					break;
				case BETWEEN:
					List<Object> values = (List<Object>) value;

					if (fieldType.equals(Date.class)) {
						predicate = criteriaBuilder.between(
								expression,
								DateUtils.parseDate(values.get(0).toString()),
								DateUtils.parseDate(values.get(1).toString())
						);
					} else if (fieldType.equals(UUID.class)) {
						predicate = criteriaBuilder.between(
								expression,
								UUID.fromString(values.get(0).toString()),
								UUID.fromString(values.get(1).toString())
						);
					} else {
						predicate = criteriaBuilder.between(expression, values.get(0).toString(), values.get(1).toString());
					}

					break;
				case IN:
					List<Object> valueList = Arrays.asList(value.toString().split(","));
					List<Object> newList = new ArrayList<Object>();

					for (Object v : valueList) {
						Object newValue = v.toString().trim();

						if (fieldType.equals(Date.class)) {
							newValue = DateUtils.parseDate(newValue.toString());
						} else if (fieldType.equals(UUID.class)) {
							newValue = UUID.fromString(newValue.toString());
						}

						newList.add(newValue);
					}

					valueList = newList;
					predicate = expression.in(valueList);

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

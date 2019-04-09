package br.inf.teorema.regen.specification;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import br.inf.teorema.regen.constants.LogicalOperator;
import org.springframework.data.jpa.domain.Specification;

import br.inf.teorema.regen.model.Condition;
import br.inf.teorema.regen.util.DateUtils;
import br.inf.teorema.regen.util.ReflectionUtils;

@SuppressWarnings("serial")
public class GenericSpecification<T> implements Specification<T> {

	private Condition condition;
	private Class<?> clazz;

	public GenericSpecification(Condition condition, Class<?> clazz) {
		super();
		this.condition = condition;
		this.clazz = clazz;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		try {
			return addCondition(this.condition, null, new ArrayList<Predicate>(), true, root, query, criteriaBuilder).get(0);
		} catch (NoSuchFieldException | ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	private List<Predicate> addCondition(
			Condition condition, LogicalOperator logicalOperator, List<Predicate> predicates, boolean last, Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder
	) throws NoSuchFieldException, ParseException {
		if (condition.getConditions() != null) {
			int i = 0;
			for (Condition subCondition : condition.getConditions()) {
				predicates = addCondition(subCondition, condition.getLogicalOperator(), predicates, i >= condition.getConditions().size() - 1, root, query, criteriaBuilder);
				i++;
			}
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
						join = root.join(f.getName());
					} else if (j < fields.size() - 1) {
						join = join.join(f.getName());
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

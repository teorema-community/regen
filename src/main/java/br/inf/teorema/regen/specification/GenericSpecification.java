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

import org.springframework.data.jpa.domain.Specification;

import br.inf.teorema.regen.model.Condition;
import br.inf.teorema.regen.util.DateUtils;
import br.inf.teorema.regen.util.ReflectionUtils;

@SuppressWarnings("serial")
public class GenericSpecification<T> implements Specification<T> {

	private List<Condition> filter;
	private Class<?> clazz;
	
	public GenericSpecification(List<Condition> filter, Class<?> clazz) {
		super();
		this.filter = filter;
		this.clazz = clazz;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		try {
			List<Predicate> predicates = new ArrayList<Predicate>();
			
			int i = 0;
			for (Condition condition : filter) {
				Object value = condition.getValue();
				if (value == null || value.toString().isEmpty()) {
					continue;
				}

				Predicate predicate = null;
				boolean last = i >= filter.size() - 1;
				Join<?, ?> join = null;
				Class<?> fieldType = null;
				String fieldName = null;
				List<Field> fields = ReflectionUtils.getFields(condition.getField(), clazz);
		
				if (fields.size() > 1) {
					int j = 0;
					for (Field f : fields) {
						if (j == 0) {
							join = root.join(f.getName());
						} else if (j < fields.size() -1) {
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
				
				Predicate[] predicateArray = null;			
				if (last) {
					predicates.add(predicate);
					predicateArray = predicates.toArray(new Predicate[0]);
				}
				
				switch (condition.getLogicalOperator()) {
					case AND:
						predicate = (last) ? criteriaBuilder.and(predicateArray) : criteriaBuilder.and(predicate);	 			
						break;
					case OR:
						predicate = (last) ? criteriaBuilder.or(predicateArray) :  criteriaBuilder.or(predicate);					
						break;
					default:
						break;						
				}
				
				if (last) {
					return predicate;
				} 
				
				predicates.add(predicate); 			
				i++;
			}
		} catch (NoSuchFieldException | ParseException e) {
			e.printStackTrace();
		} 
		
		return null;
	}

	public List<Condition> getFilter() {
		return filter;
	}

	public void setFilter(List<Condition> filter) {
		this.filter = filter;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	
}

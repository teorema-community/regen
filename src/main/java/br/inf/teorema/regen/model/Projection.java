package br.inf.teorema.regen.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.NotImplementedException;

import br.inf.teorema.regen.enums.Function;

public class Projection<N> {

	private String originalName;
	private String name;
	private String alias;
	private Function function;
	private List<String> parameters = new ArrayList<>();
	private Expression<?> expression;
	private boolean hasAlias = false;

	public Projection(String originalName) {
		this.originalName = originalName;
		this.name = originalName;
		this.alias = originalName;
		this.hasAlias = this.originalName.contains("as");
		
		if (hasAlias) {
			String[] split = originalName.split("as");
			this.name = split[0].trim();
			this.alias = split[1].trim();
		}
		
		if (this.name.contains("(")) {
			String funStr = name.substring(0, name.indexOf("("));
			this.function = Function.valueOf(funStr.toUpperCase());
			this.name = name.substring(name.indexOf("(") + 1, name.lastIndexOf(")"));
			this.parameters = Arrays.asList(name.split(",")).stream().map(p -> p.trim()).collect(Collectors.toList());
		}
	}
	
	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Function getFunction() {
		return function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}
	public List<String> getParameters() {
		return parameters;
	}
	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}
	public boolean hasFunction() {
		return function != null;
	}

	public boolean isHasAlias() {
		return hasAlias;
	}

	public void setHasAlias(boolean hasAlias) {
		this.hasAlias = hasAlias;
	}

	public <T> void applyFunction(CriteriaBuilder criteriaBuilder, Root<T> root, List<Projection> subProjections) {
		this.expression = createFunctionExpression(criteriaBuilder, root, subProjections);		
	}
	
	@SuppressWarnings("unchecked")
	private <T> Expression<?> createFunctionExpression(CriteriaBuilder criteriaBuilder, Root<T> root, List<Projection> subProjections) {
		if (hasFunction()) {
			switch (function) {
				case ABS: return criteriaBuilder.abs(subProjections.get(0).getExpression());
				case AVG: return criteriaBuilder.avg(subProjections.get(0).getExpression());
				case COUNT:	return criteriaBuilder.count(subProjections.get(0).getExpression());
				case COUNT_DISCTINT: return criteriaBuilder.countDistinct(subProjections.get(0).getExpression());
				case DIFF: return criteriaBuilder.diff(subProjections.get(0).getExpression(), subProjections.get(1).getExpression());
				case LOWER: return criteriaBuilder.lower(subProjections.get(0).getExpression());
				case MAX: return criteriaBuilder.max(subProjections.get(0).getExpression());
				case MIN: return criteriaBuilder.min(subProjections.get(0).getExpression());
				case MOD: return criteriaBuilder.mod(subProjections.get(0).getExpression(), subProjections.get(1).getExpression());
				case NEG: return criteriaBuilder.neg(subProjections.get(0).getExpression());
				case PROD: return criteriaBuilder.prod(subProjections.get(0).getExpression(), subProjections.get(1).getExpression());
				case QUOT: return criteriaBuilder.quot(subProjections.get(0).getExpression(), subProjections.get(1).getExpression());
				case SQRT: return criteriaBuilder.sqrt(subProjections.get(0).getExpression());
				case SUBSTRING: 
					if (subProjections.size() > 2) {
						return criteriaBuilder.substring(subProjections.get(0).getExpression(), subProjections.get(1).getExpression(), subProjections.get(2).getExpression());
					} else {
						return criteriaBuilder.substring(subProjections.get(0).getExpression(), subProjections.get(1).getExpression());
					}
				case SUM: return criteriaBuilder.sum(subProjections.get(0).getExpression());
				case TRIM: return criteriaBuilder.trim(subProjections.get(0).getExpression());
				case UPPER: return criteriaBuilder.upper(subProjections.get(0).getExpression());
				default: break;		
			}
			
			throw new NotImplementedException("Function " + function + " not yet implemented");
		} else {
			throw new NullPointerException("This Projection has no function");
		}		
	}

	public Expression<?> getExpression() {
		return expression;
	}

	public void setExpression(Expression<?> expression) {
		this.expression = expression;
	}
	
}
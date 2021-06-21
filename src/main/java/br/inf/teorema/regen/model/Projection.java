package br.inf.teorema.regen.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.apache.commons.lang3.NotImplementedException;

import br.inf.teorema.regen.enums.Function;

public class Projection {

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
			this.parameters = Arrays.asList(name.split(","));
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
	
	private <T> Expression<?> createFunctionExpression(CriteriaBuilder criteriaBuilder, Root<T> root, List<Projection> subProjections) {
		if (hasFunction()) {
			switch (function) {
				case ABS:
					break;
				case AVG:
					break;
				case COUNT:
					return criteriaBuilder.count(subProjections.get(0).getExpression());
				case COUNT_DISCTINT:
					break;
				case DIFF:
					break;
				case LOWER:
					break;
				case MAX:
					break;
				case MIN:
					break;
				case MOD:
					break;
				case NEG:
					break;
				case PROD:
					break;
				case QUOT:
					break;
				case SQRT:
					break;
				case SUBSTRING:
					break;
				case SUM:
					break;
				case TRIM:
					break;
				case UPPER:
					break;
				default:
					break;		
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

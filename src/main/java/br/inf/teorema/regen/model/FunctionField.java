package br.inf.teorema.regen.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.apache.commons.lang3.NotImplementedException;

import br.inf.teorema.regen.enums.Function;

public class FunctionField {

	private Function function;
	private List<String> parameters = new ArrayList<>();

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

	public <T> Expression<?> apply(CriteriaBuilder criteriaBuilder, Root<T> root, List<Expression<?>> expressions) {
		switch (function) {
			case ABS:
				break;
			case AVG:
				break;
			case COUNT:
				return criteriaBuilder.count(expressions.get(0));
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
	}
	
}

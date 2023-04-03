package br.inf.teorema.regen.enums;

import java.util.Arrays;
import java.util.Optional;

public enum ConditionalOperator {
	EQUALS("="), 
	NOT_EQUALS("!="),
	GREATER_THAN(">"), 
	LESS_THAN("<"), 
	GREATER_THAN_OR_EQUAL_TO(">="), 
	LESS_THAN_OR_EQUAL_TO("<="),
	LIKE("like"),
	LIKE_START("like start"),
	LIKE_END("like end"),
	CUSTOM_LIKE("custom like"),
	BETWEEN("between"),
	IN("in"),
	IS_NULL("is null"),
	IS_NOT_NULL("is not null");
	
	private String symbol;
	
	private ConditionalOperator(String symbol) {
		this.symbol = symbol;
	}
	
	public static Optional<ConditionalOperator> getBySymbol(String symbol) {
		return Arrays.asList(values()).stream().filter(o -> symbol.equals(o.symbol)).findFirst();
	}

	public String getSymbol() {
		return symbol;
	}
}

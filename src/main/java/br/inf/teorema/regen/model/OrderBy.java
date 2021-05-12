package br.inf.teorema.regen.model;

import br.inf.teorema.regen.enums.OrderDirection;

public class OrderBy {

    private OrderDirection direction = OrderDirection.ASC;
    private Condition condition;

    public OrderBy() {
		super();
	}

	public OrderBy(OrderDirection direction, Condition condition) {
		super();
		this.direction = direction;
		this.condition = condition;
	}
	
	public OrderBy(OrderDirection direction, String field) {
		this(direction, new Condition(field));
	}
	
	public OrderBy(String field) {
		this(OrderDirection.ASC, field);
	}

	public OrderDirection getDirection() {
        return direction;
    }

    public void setDirection(OrderDirection direction) {
        this.direction = direction;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }
}

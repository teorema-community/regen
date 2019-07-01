package br.inf.teorema.regen.model;

import br.inf.teorema.regen.enums.OrderDirection;

public class OrderBy {

    private OrderDirection direction = OrderDirection.ASC;
    private Condition condition;

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

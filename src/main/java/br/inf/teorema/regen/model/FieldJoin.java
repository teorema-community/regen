package br.inf.teorema.regen.model;

import javax.persistence.criteria.JoinType;

public class FieldJoin {

    private String field;
    private JoinType type;
    private Condition on;

    public FieldJoin() {}

    public FieldJoin(String field, JoinType type) {
        this.field = field;
        this.type = type;
    }

    public FieldJoin(String field, JoinType type, Condition on) {
        this.field = field;
        this.type = type;
        this.on = on;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public JoinType getType() {
        return type;
    }

    public void setType(JoinType type) {
        this.type = type;
    }

    public Condition getOn() {
        return on;
    }

    public void setOn(Condition on) {
        this.on = on;
    }
}

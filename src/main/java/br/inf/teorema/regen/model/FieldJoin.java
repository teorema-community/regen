package br.inf.teorema.regen.model;

import br.inf.teorema.regen.constants.ConditionalOperator;

import javax.persistence.criteria.JoinType;

public class FieldJoin {

    private String joinSourceField;
    private String joinField;
    private JoinType type = JoinType.INNER;
    private ConditionalOperator conditionalOperator = ConditionalOperator.EQUALS;
    private String onSourceConditionField;
    private String onConditionField;

    public FieldJoin() {}

    public FieldJoin(String joinField, String onSourceConditionField, String onConditionField) {
        this.joinField = joinField;
        this.onSourceConditionField = onSourceConditionField;
        this.onConditionField = onConditionField;
    }

    public FieldJoin(String joinField, JoinType type, String onSourceConditionField, String onConditionField) {
        this.joinField = joinField;
        this.type = type;
        this.onSourceConditionField = onSourceConditionField;
        this.onConditionField = onConditionField;
    }

    public FieldJoin(String joinSourceField, String joinField, String onSourceConditionField, String onConditionField) {
        this.joinSourceField = joinSourceField;
        this.joinField = joinField;
        this.onSourceConditionField = onSourceConditionField;
        this.onConditionField = onConditionField;
    }

    public FieldJoin(String joinSourceField, String joinField, JoinType type, String onSourceConditionField, String onConditionField) {
        this.joinSourceField = joinSourceField;
        this.joinField = joinField;
        this.type = type;
        this.onSourceConditionField = onSourceConditionField;
        this.onConditionField = onConditionField;
    }

    public String getJoinSourceField() {
        return joinSourceField;
    }

    public void setJoinSourceField(String joinSourceField) {
        this.joinSourceField = joinSourceField;
    }

    public String getJoinField() {
        return joinField;
    }

    public void setJoinField(String joinField) {
        this.joinField = joinField;
    }

    public JoinType getType() {
        return type;
    }

    public void setType(JoinType type) {
        this.type = type;
    }

    public ConditionalOperator getConditionalOperator() {
        return conditionalOperator;
    }

    public void setConditionalOperator(ConditionalOperator conditionalOperator) {
        this.conditionalOperator = conditionalOperator;
    }

    public String getOnSourceConditionField() {
        return onSourceConditionField;
    }

    public void setOnSourceConditionField(String onSourceConditionField) {
        this.onSourceConditionField = onSourceConditionField;
    }

    public String getOnConditionField() {
        return onConditionField;
    }

    public void setOnConditionField(String onConditionField) {
        this.onConditionField = onConditionField;
    }
}

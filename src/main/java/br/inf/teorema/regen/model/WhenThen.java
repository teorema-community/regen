package br.inf.teorema.regen.model;

public class WhenThen {

    private Condition when;
    private String expressionThen;
    private Object rawThen;

    public Condition getWhen() {
        return when;
    }

    public void setWhen(Condition when) {
        this.when = when;
    }

    public String getExpressionThen() {
        return expressionThen;
    }

    public void setExpressionThen(String expressionThen) {
        this.expressionThen = expressionThen;
    }

    public Object getRawThen() {
        return rawThen;
    }

    public void setRawThen(Object rawThen) {
        this.rawThen = rawThen;
    }
}

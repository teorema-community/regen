package br.inf.teorema.regen.model;

public class WhenThen {

    private Condition when;
    private Condition conditionalThen;
    private Object pureThen;

    public Condition getWhen() {
        return when;
    }

    public void setWhen(Condition when) {
        this.when = when;
    }

    public Condition getConditionalThen() {
        return conditionalThen;
    }

    public void setConditionalThen(Condition conditionalThen) {
        this.conditionalThen = conditionalThen;
    }

    public Object getPureThen() {
        return pureThen;
    }

    public void setPureThen(Object pureThen) {
        this.pureThen = pureThen;
    }
}

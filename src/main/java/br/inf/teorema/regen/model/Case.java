package br.inf.teorema.regen.model;

import java.util.ArrayList;
import java.util.List;

public class Case {

    private List<WhenThen> whenThens;
    private String expressionOtherwise;
    private Object rawOtherwise;

    public List<WhenThen> getWhenThens() {
        if (whenThens == null) {
            whenThens = new ArrayList<>();
        }

        return whenThens;
    }

    public void setWhenThens(List<WhenThen> whenThens) {
        this.whenThens = whenThens;
    }

    public String getExpressionOtherwise() {
        return expressionOtherwise;
    }

    public void setExpressionOtherwise(String expressionOtherwise) {
        this.expressionOtherwise = expressionOtherwise;
    }

    public Object getRawOtherwise() {
        return rawOtherwise;
    }

    public void setRawOtherwise(Object rawOtherwise) {
        this.rawOtherwise = rawOtherwise;
    }
}

package br.inf.teorema.regen.model;

import java.util.ArrayList;
import java.util.List;

public class Case {

    private List<WhenThen> whenThens;

    public List<WhenThen> getWhenThens() {
        if (whenThens == null) {
            whenThens = new ArrayList<>();
        }

        return whenThens;
    }

    public void setWhenThens(List<WhenThen> whenThens) {
        this.whenThens = whenThens;
    }
}

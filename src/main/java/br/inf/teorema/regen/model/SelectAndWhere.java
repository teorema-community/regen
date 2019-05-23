package br.inf.teorema.regen.model;

import java.util.ArrayList;
import java.util.List;

public class SelectAndWhere {

    private List<String> select;
    private Condition where;

    public List<String> getSelect() {
        if (select == null) {
            select = new ArrayList<>();
        }

        return select;
    }

    public void setSelect(List<String> select) {
        this.select = select;
    }

    public Condition getWhere() {
        return where;
    }

    public void setWhere(Condition where) {
        this.where = where;
    }
}

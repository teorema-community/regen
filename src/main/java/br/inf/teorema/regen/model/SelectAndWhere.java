package br.inf.teorema.regen.model;

import br.inf.teorema.regen.model.Condition;

import java.util.ArrayList;
import java.util.List;

public class SelectAndWhere {

    private List<String> select;
    private List<Condition> where;

    public List<String> getSelect() {
        if (select == null) {
            select = new ArrayList<>();
        }

        return select;
    }

    public void setSelect(List<String> select) {
        this.select = select;
    }

    public List<Condition> getWhere() {
        if (where == null) {
            where = new ArrayList<>();
        }

        return where;
    }

    public void setWhere(List<Condition> where) {
        this.where = where;
    }
}

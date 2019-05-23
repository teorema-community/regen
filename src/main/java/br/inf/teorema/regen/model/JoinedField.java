package br.inf.teorema.regen.model;

import javax.persistence.criteria.Join;
import java.lang.reflect.Field;

public class JoinedField {

    private Field field;
    private Field sourceField;
    private Join join;

    public JoinedField() {}

    public JoinedField(Field field, Join join) {
        this.field = field;
        this.join = join;
    }

    public JoinedField(Field field, Field sourceField, Join join) {
        this.field = field;
        this.sourceField = sourceField;
        this.join = join;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Field getSourceField() {
        return sourceField;
    }

    public void setSourceField(Field sourceField) {
        this.sourceField = sourceField;
    }

    public Join getJoin() {
        return join;
    }

    public void setJoin(Join join) {
        this.join = join;
    }
}

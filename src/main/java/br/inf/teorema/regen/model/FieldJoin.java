package br.inf.teorema.regen.model;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FieldJoin {

    private String sourceField;
    private String field;
    private JoinType type = JoinType.INNER;
    private String alias;
    private Condition on;
    
    @JsonIgnore
    private Join<?, ?> join;

    public FieldJoin() {}

    public FieldJoin(String field) {
        this.field = field;
    }

    public FieldJoin(String field, JoinType type) {
        this.field = field;
        this.type = type;
    }

    public FieldJoin(String sourceField, String field) {
        this.sourceField = sourceField;
        this.field = field;
    }

    public FieldJoin(String sourceField, String field, JoinType type) {
        this.sourceField = sourceField;
        this.field = field;
        this.type = type;
    }

	public FieldJoin(String sourceField, String field, JoinType type, String alias, Condition on, Join<?, ?> join) {
		super();
		this.sourceField = sourceField;
		this.field = field;
		this.type = type;
		this.alias = alias;
		this.join = join;
		this.on = on;
	}

	public FieldJoin(String sourceField, String field, JoinType type, String alias) {
		super();
		this.sourceField = sourceField;
		this.field = field;
		this.type = type;
		this.alias = alias;
	}

	public FieldJoin(String sourceField, String field, JoinType type, String alias, Condition on) {
		super();
		this.sourceField = sourceField;
		this.field = field;
		this.type = type;
		this.alias = alias;
		this.on = on;
	}

	public String getSourceField() {
        return sourceField;
    }

    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
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

	public Join<?, ?> getJoin() {
		return join;
	}

	public void setJoin(Join<?, ?> join) {
		this.join = join;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Condition getOn() {
		return on;
	}

	public void setOn(Condition on) {
		this.on = on;
	}
	
}

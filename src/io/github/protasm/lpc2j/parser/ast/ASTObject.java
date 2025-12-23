package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ASTObject extends ASTNode {
    private String parentName;
    private final String name;
    private final List<ASTInherit> inherits;
    private final ASTFields fields;
    private final ASTMethods methods;

    public ASTObject(int line, String name) {
        super(line);

        this.name = name;

        parentName = null;
        inherits = new ArrayList<>();
        fields = new ASTFields(line);
        methods = new ASTMethods(line);
    }

    public List<ASTInherit> inherits() {
        return Collections.unmodifiableList(inherits);
    }

    public void addInherit(ASTInherit inherit) {
        inherits.add(inherit);
        if (parentName == null)
            parentName = inherit.path();
    }

    public String parentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String name() {
        return name;
    }

    public ASTFields fields() {
        return fields;
    }

    public ASTMethods methods() {
        return methods;
    }
}

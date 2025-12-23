package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.IdentifierResolution;
import io.github.protasm.lpc2j.parser.type.LPCType;
import java.util.Objects;

/** Assignment to an identifier prior to binding. */
public final class ASTExprIdentifierStore extends ASTExpression {
    public enum AssignmentOp {
        ASSIGN,
        PLUS_EQUAL,
        MINUS_EQUAL
    }

    private final String name;
    private final AssignmentOp operator;
    private final ASTExpression value;
    private IdentifierResolution resolution;
    private LPCType lpcType;

    public ASTExprIdentifierStore(int line, String name, AssignmentOp operator, ASTExpression value) {
        super(line);
        this.name = Objects.requireNonNull(name, "name");
        this.operator = Objects.requireNonNull(operator, "operator");
        this.value = Objects.requireNonNull(value, "value");
    }

    public String name() {
        return name;
    }

    public AssignmentOp operator() {
        return operator;
    }

    public ASTExpression value() {
        return value;
    }

    public IdentifierResolution resolution() {
        return resolution;
    }

    public void setResolution(IdentifierResolution resolution) {
        this.resolution = resolution;
    }

    public void setLpcType(LPCType lpcType) {
        this.lpcType = lpcType;
    }

    @Override
    public LPCType lpcType() {
        if (lpcType != null)
            return lpcType;

        if (resolution != null)
            return resolution.lpcType();

        return LPCType.LPCMIXED;
    }
}

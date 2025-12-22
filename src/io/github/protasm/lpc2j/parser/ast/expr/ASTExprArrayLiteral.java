package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.type.LPCType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ASTExprArrayLiteral extends ASTExpression {
    private final List<ASTExpression> elements;

    public ASTExprArrayLiteral(int line, List<ASTExpression> elements) {
        super(line);
        this.elements = (elements != null) ? new ArrayList<>(elements) : new ArrayList<>();
    }

    public List<ASTExpression> elements() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCARRAY;
    }
}

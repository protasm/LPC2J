package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.type.LPCType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ASTExprMappingLiteral extends ASTExpression {
    private final List<ASTExprMappingEntry> entries;

    public ASTExprMappingLiteral(int line, List<ASTExprMappingEntry> entries) {
        super(line);
        this.entries = new ArrayList<>(Objects.requireNonNull(entries, "entries"));
    }

    public List<ASTExprMappingEntry> entries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCMAPPING;
    }
}

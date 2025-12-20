package io.github.protasm.lpc2j.parser.ast;

public final class ASTParameters extends ASTListNode<ASTParameter> {
    public ASTParameters(int line) {
        super(line);
    }

    public String descriptor() {
        StringBuilder sb = new StringBuilder();

        for (ASTParameter param : nodes)
            sb.append(param.descriptor());

        return "(" + sb.toString().trim() + ")";
    }
}

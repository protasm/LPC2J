package io.github.protasm.lpc2j.pipeline;

import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.semantic.SemanticModel;
import io.github.protasm.lpc2j.token.TokenList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CompilationResult {
    private final TokenList tokens;
    private final ASTObject astObject;
    private final SemanticModel semanticModel;
    private final byte[] bytecode;
    private final List<CompilationProblem> problems;

    public CompilationResult(
            TokenList tokens,
            ASTObject astObject,
            SemanticModel semanticModel,
            byte[] bytecode,
            List<CompilationProblem> problems) {
        this.tokens = tokens;
        this.astObject = astObject;
        this.semanticModel = semanticModel;
        this.bytecode = bytecode;
        this.problems =
                Collections.unmodifiableList(
                        Objects.requireNonNull(problems, "problems"));
    }

    public boolean succeeded() {
        return problems.isEmpty() && bytecode != null;
    }

    public TokenList getTokens() {
        return tokens;
    }

    public ASTObject getAstObject() {
        return astObject;
    }

    public SemanticModel getSemanticModel() {
        return semanticModel;
    }

    public byte[] getBytecode() {
        return bytecode;
    }

    public List<CompilationProblem> getProblems() {
        return problems;
    }
}

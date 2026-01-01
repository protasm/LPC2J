package io.github.protasm.lpc2j.pipeline;

import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.semantic.SemanticModel;
import io.github.protasm.lpc2j.token.TokenList;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Per-file compilation metadata and analysis artifacts.
 *
 * <p>The compilation unit tracks the originating source identifiers, the inherited object path (if
 * any), a link to the resolved parent unit, and the parsed/semantic products required by later
 * stages.</p>
 */
public final class CompilationUnit {
    private final Path sourcePath;
    private final String sourceName;
    private final String displayPath;
    private final String source;
    private String inheritedPath;
    private CompilationUnit parentUnit;
    private TokenList tokens;
    private ASTObject astObject;
    private SemanticModel semanticModel;

    public CompilationUnit(Path sourcePath, String sourceName, String displayPath, String source) {
        this.sourcePath = (sourcePath != null) ? sourcePath.normalize() : null;
        this.sourceName = sourceName;
        this.displayPath = displayPath;
        this.source = Objects.requireNonNull(source, "source");
    }

    public Path sourcePath() {
        return sourcePath;
    }

    public String sourceName() {
        return sourceName;
    }

    public String displayPath() {
        return displayPath;
    }

    public String source() {
        return source;
    }

    public String parseName() {
        if (sourceName != null)
            return sourceName;

        if (displayPath != null)
            return displayPath;

        if (sourcePath != null)
            return sourcePath.toString();

        return "<input>";
    }

    public String inheritedPath() {
        return inheritedPath;
    }

    public void setInheritedPath(String inheritedPath) {
        this.inheritedPath = inheritedPath;
    }

    public CompilationUnit parentUnit() {
        return parentUnit;
    }

    public void setParentUnit(CompilationUnit parentUnit) {
        this.parentUnit = parentUnit;
    }

    public TokenList tokens() {
        return tokens;
    }

    public void setTokens(TokenList tokens) {
        this.tokens = tokens;
    }

    public ASTObject astObject() {
        return astObject;
    }

    public void setASTObject(ASTObject astObject) {
        this.astObject = astObject;
    }

    public SemanticModel semanticModel() {
        return semanticModel;
    }

    public void setSemanticModel(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }
}

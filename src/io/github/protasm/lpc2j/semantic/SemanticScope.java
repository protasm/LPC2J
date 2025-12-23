package io.github.protasm.lpc2j.semantic;

import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.pipeline.CompilationUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Represents a lexical scope containing symbol declarations. */
public final class SemanticScope {
    private final SemanticScope parent;
    private final Map<String, List<ScopedSymbol>> symbols = new LinkedHashMap<>();

    public SemanticScope() {
        this(null);
    }

    public SemanticScope(SemanticScope parent) {
        this.parent = parent;
    }

    public SemanticScope parent() {
        return parent;
    }

    public void declare(Symbol symbol) {
        declare(symbol, null, null, null);
    }

    public void declare(Symbol symbol, CompilationUnit originUnit, ASTField field, ASTMethod method) {
        if (symbol == null)
            return;

        symbols.computeIfAbsent(symbol.name(), ignored -> new ArrayList<>())
                .add(new ScopedSymbol(symbol, originUnit, field, method));
    }

    public void importSymbol(ScopedSymbol scopedSymbol) {
        if (scopedSymbol == null)
            return;

        symbols.computeIfAbsent(scopedSymbol.symbol().name(), ignored -> new ArrayList<>())
                .add(scopedSymbol);
    }

    public ScopedSymbol resolve(String name) {
        List<ScopedSymbol> local = symbols.get(name);
        if (local != null && !local.isEmpty())
            return local.get(local.size() - 1);

        if (parent != null)
            return parent.resolve(name);

        return null;
    }

    public ScopedSymbol resolveLocally(String name) {
        List<ScopedSymbol> local = symbols.get(name);
        if (local == null || local.isEmpty())
            return null;

        return local.get(local.size() - 1);
    }

    public List<ScopedSymbol> resolveAll(String name) {
        List<ScopedSymbol> resolved = new ArrayList<>();
        SemanticScope scope = this;

        while (scope != null) {
            List<ScopedSymbol> local = scope.symbols.get(name);
            if (local != null)
                resolved.addAll(local);
            scope = scope.parent;
        }

        return Collections.unmodifiableList(resolved);
    }

    public Map<String, List<ScopedSymbol>> symbols() {
        return Collections.unmodifiableMap(symbols);
    }

    public static final class ScopedSymbol {
        private final Symbol symbol;
        private final CompilationUnit originUnit;
        private final ASTField field;
        private final ASTMethod method;

        public ScopedSymbol(Symbol symbol, CompilationUnit originUnit, ASTField field, ASTMethod method) {
            this.symbol = symbol;
            this.originUnit = originUnit;
            this.field = field;
            this.method = method;
        }

        public Symbol symbol() {
            return symbol;
        }

        public CompilationUnit originUnit() {
            return originUnit;
        }

        public ASTField field() {
            return field;
        }

        public ASTMethod method() {
            return method;
        }
    }
}

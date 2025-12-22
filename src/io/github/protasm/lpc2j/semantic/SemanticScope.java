package io.github.protasm.lpc2j.semantic;

import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.pipeline.CompilationUnit;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Represents a lexical scope containing symbol declarations. */
public final class SemanticScope {
    private final SemanticScope parent;
    private final Map<String, SymbolBinding> symbols = new LinkedHashMap<>();

    public SemanticScope() {
        this(null);
    }

    public SemanticScope(SemanticScope parent) {
        this.parent = parent;
    }

    public SemanticScope parent() {
        return parent;
    }

    public SymbolBinding declare(Symbol symbol, CompilationUnit originUnit) {
        return declare(symbol, originUnit, false, null, null);
    }

    public SymbolBinding declareInherited(Symbol symbol, CompilationUnit originUnit) {
        return declare(symbol, originUnit, true, null, null);
    }

    public SymbolBinding declare(Symbol symbol, CompilationUnit originUnit, boolean inherited, SymbolBinding inheritedFrom, ASTMethod method) {
        if (symbol == null)
            return null;

        SymbolBinding binding = new SymbolBinding(symbol, originUnit, inherited, inheritedFrom, method);
        symbols.put(symbol.name(), binding);
        return binding;
    }

    public SymbolBinding resolve(String name) {
        SymbolBinding found = symbols.get(name);

        if (found != null)
            return found;

        if (parent != null)
            return parent.resolve(name);

        return null;
    }

    public SymbolBinding resolveLocally(String name) {
        return symbols.get(name);
    }

    public Map<String, SymbolBinding> symbols() {
        return Collections.unmodifiableMap(symbols);
    }

    /** Captures a symbol within a semantic scope along with its origin and inheritance metadata. */
    public static final class SymbolBinding {
        private final Symbol symbol;
        private final CompilationUnit originUnit;
        private final boolean inherited;
        private final SymbolBinding inheritedFrom;
        private final ASTMethod method;
        private ASTMethod overriddenMethod;

        private SymbolBinding(Symbol symbol, CompilationUnit originUnit, boolean inherited, SymbolBinding inheritedFrom, ASTMethod method) {
            this.symbol = symbol;
            this.originUnit = originUnit;
            this.inherited = inherited;
            this.inheritedFrom = inheritedFrom;
            this.method = method;
        }

        public Symbol symbol() {
            return symbol;
        }

        public CompilationUnit originUnit() {
            return originUnit;
        }

        public boolean inherited() {
            return inherited;
        }

        public SymbolBinding inheritedFrom() {
            return inheritedFrom;
        }

        public ASTMethod method() {
            return method;
        }

        public ASTMethod overriddenMethod() {
            return overriddenMethod;
        }

        public void setOverriddenMethod(ASTMethod overriddenMethod) {
            this.overriddenMethod = overriddenMethod;
        }
    }
}

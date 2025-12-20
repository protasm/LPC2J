package io.github.protasm.lpc2j.semantic;

import io.github.protasm.lpc2j.parser.ast.Symbol;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Represents a lexical scope containing symbol declarations. */
public final class SemanticScope {
    private final SemanticScope parent;
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();

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
        if (symbol == null)
            return;

        symbols.put(symbol.name(), symbol);
    }

    public Symbol resolve(String name) {
        Symbol found = symbols.get(name);

        if (found != null)
            return found;

        if (parent != null)
            return parent.resolve(name);

        return null;
    }

    public Map<String, Symbol> symbols() {
        return Collections.unmodifiableMap(symbols);
    }
}

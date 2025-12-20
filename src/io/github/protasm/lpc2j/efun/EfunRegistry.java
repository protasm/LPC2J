package io.github.protasm.lpc2j.efun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Instance-scoped efun registry supporting signature-aware lookups. */
public final class EfunRegistry {
    private final Map<String, List<Efun>> registry = new HashMap<>();

    public void register(Efun efun) {
        Objects.requireNonNull(efun, "efun");
        registry.computeIfAbsent(efun.signature().name(), k -> new ArrayList<>()).add(efun);
    }

    public Efun lookup(String name, int arity) {
        List<Efun> efuns = registry.get(name);

        if (efuns == null)
            return null;

        Efun match = null;
        for (Efun efun : efuns) {
            if (efun.arity() != arity)
                continue;

            if (match != null)
                throw new IllegalStateException("Ambiguous efun overload for '" + name + "' with arity " + arity);

            match = efun;
        }

        return match;
    }

    public List<EfunSignature> signatures(String name) {
        return registry.getOrDefault(name, List.of()).stream().map(Efun::signature).toList();
    }
}

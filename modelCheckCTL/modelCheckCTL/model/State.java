package modelCheckCTL.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** An identified state with the atomic propositions true in that state. */
public final class State {
    private final String id;
    private final Set<String> atoms;

    public State(String id, Set<String> atoms) {
        this.id = id;
        this.atoms = Collections.unmodifiableSet(new LinkedHashSet<>(atoms));
    }
    public String id() { return id; }
    public Set<String> atoms() { return atoms; }
    public boolean hasAtom(String name) { return atoms.contains(name); }
    @Override public String toString() { return id + " " + atoms; }
}

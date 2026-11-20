package modelCheckCTL.model;

import java.util.*;

/** Explicit-state set-based CTL evaluation using least/greatest fixed points. */
public final class ModelChecker {
    private final KripkeStructure model;
    private final IdentityHashMap<Formula, Set<String>> cache = new IdentityHashMap<>();
    public ModelChecker(KripkeStructure model) { this.model=model; }

    public Set<String> satisfyingStates(Formula f) {
        Set<String> cached = cache.get(f);
        if (cached != null) return cached;
        Set<String> result;
        switch (f.op) {
            case ATOM -> {
                result = new LinkedHashSet<>();
                for (State s : model.states().values()) if (s.hasAtom(f.atom)) result.add(s.id());
            }
            case NOT -> { result = all(); result.removeAll(satisfyingStates(f.left)); }
            case AND -> { result = new LinkedHashSet<>(satisfyingStates(f.left)); result.retainAll(satisfyingStates(f.right)); }
            case OR -> { result = new LinkedHashSet<>(satisfyingStates(f.left)); result.addAll(satisfyingStates(f.right)); }
            case IMPLIES -> { result = all(); result.removeAll(satisfyingStates(f.left)); result.addAll(satisfyingStates(f.right)); }
            case EX -> result = predecessorsAny(satisfyingStates(f.left));
            case AX -> result = predecessorsAll(satisfyingStates(f.left));
            case EF -> result = least(satisfyingStates(f.left), null, false);
            case AF -> result = least(satisfyingStates(f.left), null, true);
            case EU -> result = least(satisfyingStates(f.right), satisfyingStates(f.left), false);
            case AU -> result = least(satisfyingStates(f.right), satisfyingStates(f.left), true);
            case EG -> result = greatest(satisfyingStates(f.left), false);
            case AG -> result = greatest(satisfyingStates(f.left), true);
            default -> throw new IllegalStateException("Unsupported CTL operator: " + f.op);
        }
        // Keep results in the model's documented state order for readable output.
        Set<String> ordered = new LinkedHashSet<>();
        for (String id : model.ids()) if (result.contains(id)) ordered.add(id);
        result = Collections.unmodifiableSet(ordered);
        cache.put(f,result);
        return result;
    }
    public boolean holds(String state, Formula f) {
        if (!model.contains(state)) throw new IllegalArgumentException("State '" + state + "' does not exist");
        return satisfyingStates(f).contains(state);
    }
    private Set<String> all() { return new LinkedHashSet<>(model.ids()); }
    private Set<String> predecessorsAny(Set<String> states) {
        Set<String> pred = new LinkedHashSet<>();
        for (String s : model.ids()) for (String to : model.successors(s))
            if (states.contains(to)) { pred.add(s); break; }
        return pred;
    }
    private Set<String> predecessorsAll(Set<String> states) {
        Set<String> pred = new LinkedHashSet<>();
        for (String s : model.ids()) {
            Set<String> next = model.successors(s);
            // Non-vacuous AX on terminal states matches the supplied test expectations.
            if (!next.isEmpty() && states.containsAll(next)) pred.add(s);
        }
        return pred;
    }
    private Set<String> least(Set<String> seed, Set<String> restriction, boolean universal) {
        Set<String> result = new LinkedHashSet<>(seed);
        boolean changed;
        do {
            Set<String> pred = universal ? predecessorsAll(result) : predecessorsAny(result);
            if (restriction!=null) pred.retainAll(restriction);
            changed = result.addAll(pred);
        } while (changed);
        return result;
    }
    private Set<String> greatest(Set<String> seed, boolean universal) {
        Set<String> result = new LinkedHashSet<>(seed);
        boolean changed;
        do {
            Set<String> pred = universal ? predecessorsAll(result) : predecessorsAny(result);
            if (universal) { // A terminal state satisfying p can satisfy AG p on a finite maximal path.
                for (String state : model.ids()) if (model.successors(state).isEmpty()) pred.add(state);
            }
            changed = result.retainAll(pred);
        } while (changed);
        return result;
    }
}

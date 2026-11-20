package modelCheckCTL.model;

import java.util.*;

/** Immutable Kripke graph. The input files specify no initial-state set. */
public final class KripkeStructure {
    private final Map<String, State> states;
    private final List<Transition> transitions;
    private final Map<String, Set<String>> outgoing;

    public KripkeStructure(Map<String, State> states, List<Transition> transitions) {
        this.states = Collections.unmodifiableMap(new LinkedHashMap<>(states));
        this.transitions = List.copyOf(transitions);
        Map<String, Set<String>> temp = new LinkedHashMap<>();
        for (String id : states.keySet()) temp.put(id, new LinkedHashSet<>());
        for (Transition edge : transitions) temp.get(edge.from()).add(edge.to());
        Map<String, Set<String>> frozen = new LinkedHashMap<>();
        for (var item : temp.entrySet()) frozen.put(item.getKey(), Collections.unmodifiableSet(item.getValue()));
        outgoing = Collections.unmodifiableMap(frozen);
    }
    public Map<String, State> states() { return states; }
    public List<Transition> transitions() { return transitions; }
    public Set<String> successors(String state) { return outgoing.getOrDefault(state, Set.of()); }
    public boolean contains(String state) { return states.containsKey(state); }
    public Set<String> ids() { return states.keySet(); }
}

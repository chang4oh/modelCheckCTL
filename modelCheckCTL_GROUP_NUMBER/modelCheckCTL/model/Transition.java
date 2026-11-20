package modelCheckCTL.model;

/** A named, directed transition from one state to another. */
public record Transition(String name, String from, String to) { }

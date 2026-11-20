package modelCheckCTL.util;

import modelCheckCTL.model.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;

/** Parses the three semicolon-delimited model sections independently of the example source. */
public final class KripkeParser {
    private static final Pattern STATE_ID = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");
    private static final Pattern ATOM_ID = Pattern.compile("[A-Za-z0-9_]+");
    private static final Pattern EDGE = Pattern.compile("([A-Za-z][A-Za-z0-9_]*)\\s*:\\s*([A-Za-z][A-Za-z0-9_]*)\\s*[-–]\\s*([A-Za-z][A-Za-z0-9_]*)");
    private static final Pattern LABEL = Pattern.compile("([A-Za-z][A-Za-z0-9_]*)\\s*:\\s*(.*)", Pattern.DOTALL);
    private record Segment(String content, int line) { }
    private record Entry(String value, int line) { }
    private KripkeParser() { }

    public static KripkeStructure parseFile(Path path) throws IOException, ModelFormatException {
        return parse(Files.readString(path, StandardCharsets.UTF_8));
    }
    public static KripkeStructure parse(String source) throws ModelFormatException {
        source = source.replace("\uFEFF", "");
        List<Segment> parts = sections(source);
        if (parts.size() != 3) throw new ModelFormatException(1,
                "Expected three sections: states; transitions; state labels;");
        var orderedIds = new LinkedHashSet<String>();
        for (Entry entry : entries(parts.get(0))) {
            if (!STATE_ID.matcher(entry.value()).matches()) throw new ModelFormatException(entry.line(), "Invalid state ID: " + entry.value());
            if (!orderedIds.add(entry.value())) throw new ModelFormatException(entry.line(), "State " + entry.value() + " is defined more than once");
        }
        if (orderedIds.isEmpty()) throw new ModelFormatException(parts.get(0).line(), "No states are defined");
        List<Transition> edges = new ArrayList<>();
        Set<String> edgeNames = new HashSet<>(), edgePairs = new HashSet<>();
        for (Entry entry : entries(parts.get(1))) {
            Matcher m = EDGE.matcher(entry.value());
            if (!m.matches()) throw new ModelFormatException(entry.line(), "Invalid transition format: " + entry.value());
            String name = m.group(1), src = m.group(2), dst = m.group(3);
            if (!orderedIds.contains(src) || !orderedIds.contains(dst))
                throw new ModelFormatException(entry.line(), "Transition " + name + " references an undefined state");
            if (!edgeNames.add(name)) throw new ModelFormatException(entry.line(), "Duplicate transition name: " + name);
            if (!edgePairs.add(src + "\u0000" + dst)) throw new ModelFormatException(entry.line(), "Duplicate transition from " + src + " to " + dst);
            edges.add(new Transition(name, src, dst));
        }
        Map<String, State> states = new LinkedHashMap<>();
        for (Entry entry : entries(parts.get(2))) {
            Matcher m = LABEL.matcher(entry.value());
            if (!m.matches()) throw new ModelFormatException(entry.line(), "Invalid state label entry: " + entry.value());
            String stateId = m.group(1);
            if (!orderedIds.contains(stateId)) throw new ModelFormatException(entry.line(), "State " + stateId + " is not defined");
            if (states.containsKey(stateId)) throw new ModelFormatException(entry.line(), "Duplicate labels for state " + stateId);
            Set<String> atoms = new LinkedHashSet<>();
            for (String atom : m.group(2).strip().split("\\s+")) {
                if (atom.isEmpty()) continue;
                if (!ATOM_ID.matcher(atom).matches()) throw new ModelFormatException(entry.line(), "Invalid atom: " + atom);
                if (!atoms.add(atom)) throw new ModelFormatException(entry.line(), "Atom " + atom + " is defined more than once for state " + stateId);
            }
            states.put(stateId, new State(stateId, atoms));
        }
        for (String id : orderedIds) if (!states.containsKey(id))
            throw new ModelFormatException(parts.get(2).line(), "Missing propositional labels for state " + id);
        Map<String, State> ordered = new LinkedHashMap<>();
        for (String id : orderedIds) ordered.put(id, states.get(id));
        return new KripkeStructure(ordered, edges);
    }
    private static List<Segment> sections(String input) throws ModelFormatException {
        List<Segment> out = new ArrayList<>();
        int start = 0, line = 1, segmentLine = 1;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == ';') {
                String part = input.substring(start, i).trim();
                if (!part.isEmpty()) out.add(new Segment(input.substring(start, i), segmentLine));
                start = i + 1; segmentLine = line;
            }
            if (c == '\n') line++;
        }
        if (!input.substring(start).trim().isEmpty())
            throw new ModelFormatException(line, "Missing semicolon at the end of a section");
        return out;
    }
    private static List<Entry> entries(Segment part) {
        List<Entry> result = new ArrayList<>();
        int last = 0, line = part.line();
        String str = part.content();
        for (int i = 0; i <= str.length(); i++) {
            if (i == str.length() || str.charAt(i) == ',') {
                String raw = str.substring(last, i);
                String v = raw.trim();
                int entryLine = line;
                // Leading whitespace can contain the newline after the preceding section.
                for (int j = 0; j < raw.length() && Character.isWhitespace(raw.charAt(j)); j++)
                    if (raw.charAt(j) == '\n') entryLine++;
                if (!v.isEmpty()) result.add(new Entry(v, entryLine));
                for (int j = last; j < i; j++) if (str.charAt(j) == '\n') line++;
                last = i + 1;
            }
        }
        return result;
    }
}

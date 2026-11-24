package modelCheckCTL.controller;

import modelCheckCTL.model.*;
import modelCheckCTL.util.*;
import modelCheckCTL.view.MainWindow;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

/** Coordinates file loading, syntax checks, and evaluation for the Swing UI. */
public final class MainController {
    private final MainWindow window;
    private KripkeStructure model;
    public MainController(MainWindow window) {
        this.window=window;
        window.onOpen(this::openModel);
        window.onCheck(this::checkFormula);
    }
    private void openModel() {
        Path path = window.chooseFile();
        if (path==null) return;
        try { load(path); }
        catch (Exception ex) { window.showError(ex.getMessage()); }
    }
    public void load(Path path) throws Exception {
        KripkeStructure candidate=KripkeParser.parseFile(path);
        model=candidate;
        String summary="States (" + candidate.states().size() + "):\n";
        for (State s : candidate.states().values()) summary += "  " + s.id() + " : " + String.join(" ",s.atoms()) + "\n";
        summary += "\nTransitions ("+candidate.transitions().size()+"):\n";
        for (Transition t:candidate.transitions()) summary += "  " + t.name()+" : "+t.from()+" -> "+t.to()+"\n";
        window.setModel(path,candidate.ids(),summary);
    }
    private void checkFormula() {
        if (model==null) { window.showError("Please load a Kripke model first."); return; }
        try {
            String state=window.currentState();
            Formula parsed=FormulaParser.parse(window.currentFormula());
            ModelChecker checker=new ModelChecker(model);
            Set<String> satisfying=checker.satisfyingStates(parsed);
            boolean holds=checker.holds(state,parsed);
            window.setOutput("Property " + (holds ? "HOLDS" : "DOES NOT HOLD") + " in state " + state + "\n\n"
                    + "Formula: " + window.currentFormula() + "\n"
                    + "Satisfying states: " + satisfying.stream().collect(Collectors.joining(", ")));
        } catch (Exception ex) { window.showError(ex.getMessage()); }
    }
}

package modelCheckCTL.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import modelCheckCTL.model.Formula;
import modelCheckCTL.model.KripkeStructure;
import modelCheckCTL.model.ModelChecker;
import modelCheckCTL.util.FormulaParser;
import modelCheckCTL.util.KripkeParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests against known models and expected CTL semantics. */
class ModelCheckerTest {
    private ModelChecker checker;

    @BeforeEach
    void loadModel1() throws Exception {
        checker = new ModelChecker(KripkeParser.parseFile(Path.of("tests/fixtures/Model 1.txt")));
    }

    private boolean holds(String state, String expression) throws Exception {
        return checker.holds(state, FormulaParser.parse(expression));
    }

    @Test
    void exFindsAnyImmediateSuccessor() throws Exception {
        assertTrue(holds("s1", "EXq"));
        assertFalse(holds("s2", "EXq"));
        assertEquals(Set.of("s1", "s4"), checker.satisfyingStates(FormulaParser.parse("EXq")));
    }

    @Test
    void axRequiresAllImmediateSuccessors() throws Exception {
        assertFalse(holds("s1", "AXq"));
        assertTrue(holds("s4", "AXq"));
    }

    @Test
    void efUsesReachabilityAcrossMultipleTransitions() throws Exception {
        assertTrue(holds("s1", "EFr"));
        assertTrue(holds("s3", "EFr"));
    }

    @Test
    void afRequiresEventualityOnEveryPath() throws Exception {
        assertTrue(holds("s1", "AFr"));
        assertTrue(holds("s4", "AFr"));
    }

    @Test
    void egRejectsPathsThatCannotStayInP() throws Exception {
        assertFalse(holds("s1", "EGp"));
        assertFalse(holds("s4", "EGp"));
    }

    @Test
    void bothUntilOperatorsHandleModel1() throws Exception {
        assertTrue(holds("s1", "A(pUq)"));
        assertTrue(holds("s1", "E(pUq)"));
        assertFalse(holds("s3", "A(pUq)"));
        assertFalse(holds("s3", "E(pUq)"));
    }

    @Test
    void nonexistentStateIsRejected() throws Exception {
        Formula formula = FormulaParser.parse("EXq");
        assertThrows(IllegalArgumentException.class, () -> checker.holds("s99", formula));
    }

    @Test
    void model4CriticalSectionPropertyHoldsEverywhere() throws Exception {
        KripkeStructure model = KripkeParser.parseFile(Path.of("tests/fixtures/Model 4.txt"));
        Formula property = FormulaParser.parse("AG(t1 -> AF c1)");
        Set<String> expected = new LinkedHashSet<>();
        for (int i = 1; i <= 9; i++) expected.add("s" + i);
        assertEquals(expected, new ModelChecker(model).satisfyingStates(property));
    }

    @Test
    void microwaveLivenessCounterexample() throws Exception {
        KripkeStructure model = KripkeParser.parseFile(Path.of("tests/microwave/microwave.txt"));
        ModelChecker microwaveChecker = new ModelChecker(model);
        assertFalse(microwaveChecker.holds("s1", FormulaParser.parse("AG(Start -> AF Heat)")));
    }

    @Test
    void terminalStateAxConventionMatchesSuppliedTests() throws Exception {
        KripkeStructure model = KripkeParser.parseFile(Path.of("tests/fixtures/Model 5.txt"));
        // Model 5 contains a terminal state s4 with p true; this project uses non-vacuous AX.
        assertFalse(new ModelChecker(model).holds("s4", FormulaParser.parse("AXp")));
    }
}

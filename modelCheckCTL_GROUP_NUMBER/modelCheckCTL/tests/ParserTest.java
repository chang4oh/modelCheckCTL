package modelCheckCTL.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import modelCheckCTL.model.Formula;
import modelCheckCTL.model.KripkeStructure;
import modelCheckCTL.util.FormulaParser;
import modelCheckCTL.util.KripkeParser;
import modelCheckCTL.util.ModelFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Syntax, malformed-file and input-structure validation tests. */
class ParserTest {
    @Test
    void model1ContainsExpectedGraph() throws Exception {
        KripkeStructure model = KripkeParser.parseFile(Path.of("tests/fixtures/Model 1.txt"));
        assertEquals(4, model.states().size());
        assertEquals(5, model.transitions().size());
        assertEquals(2, model.successors("s1").size());
        assertTrue(model.states().get("s1").hasAtom("p"));
        assertFalse(model.states().get("s3").hasAtom("p"));
    }

    @Test
    void formulaParserBuildsExpectedAbstractSyntaxTree() throws Exception {
        Formula formula = FormulaParser.parse("AG(t1 -> AF c1)");
        assertEquals(Formula.Op.AG, formula.op);
        assertEquals(Formula.Op.IMPLIES, formula.left.op);
        assertEquals(Formula.Op.AF, formula.left.right.op);
        assertEquals("c1", formula.left.right.left.atom);
    }

    @Test
    void untilSupportsParenthesesAndBrackets() throws Exception {
        assertEquals(Formula.Op.EU, FormulaParser.parse("E(pUq)").op);
        assertEquals(Formula.Op.AU, FormulaParser.parse("A[p U q]").op);
    }

    @ParameterizedTest(name = "Broken Model {0} is rejected")
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8})
    void brokenModelsProduceLineNumberedErrors(int number) {
        Path input = Path.of("tests/fixtures/Broken Model " + number + ".txt");
        ModelFormatException error = assertThrows(ModelFormatException.class, () -> KripkeParser.parseFile(input));
        assertTrue(error.line() >= 1);
        assertTrue(error.getMessage().contains("Line "));
    }

    @ParameterizedTest(name = "Formula {0} is rejected")
    @ValueSource(strings = {"Gp", "AGAp", "AGp or not", "EFGr", "A not G not p", "F(r U q)", "AEFt"})
    void invalidFormulasHaveColumnNumbers(String expression) {
        FormulaParser.SyntaxException error = assertThrows(
            FormulaParser.SyntaxException.class, () -> FormulaParser.parse(expression));
        assertTrue(error.getMessage().contains("Column "));
    }

    @Test
    void duplicateStateIsRejected() {
        String model = "s1, s1; t1 : s1 - s1; s1 : p;";
        assertThrows(ModelFormatException.class, () -> KripkeParser.parse(model));
    }

    @Test
    void undefinedTransitionStateIsRejected() {
        String model = "s1; t1 : s1 - s9; s1 : p;";
        assertThrows(ModelFormatException.class, () -> KripkeParser.parse(model));
    }
}

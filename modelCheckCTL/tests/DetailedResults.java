package modelCheckCTL.tests;

import modelCheckCTL.model.*;
import modelCheckCTL.util.*;
import java.nio.file.*;
import java.util.*;

/** Writes exact state/formula/expected/actual test results for auditability. */
public final class DetailedResults {
    private static String csv(String value) { return "\"" + value.replace("\"", "\"\"") + "\""; }
    public static void main(String[] args) throws Exception {
        Path root=Path.of(args[0]), target=Path.of(args[1]);
        List<String> rows=new ArrayList<>();
        rows.add("model,state,formula,expected,actual,status");
        int pass=0,fail=0;
        for(int number=1;number<=8;number++) {
            boolean micro=number==8;
            Path modelFile=micro ? root.resolve("microwave/microwave.txt") : root.resolve("fixtures/Model "+number+".txt");
            Path formulasFile=micro ? root.resolve("microwave/microwave-formulas.txt") : root.resolve("fixtures/Model "+number+" - Test Formulas.txt");
            KripkeStructure model=KripkeParser.parseFile(modelFile);
            ModelChecker checker=new ModelChecker(model);
            for(String line:Files.readAllLines(formulasFile)) {
                line=line.replace("\uFEFF", "").trim(); if(line.isEmpty()) continue;
                String[] parts=line.split(";",3);
                String state=parts[0].strip(), expression=parts[1].strip();
                boolean expected=Boolean.parseBoolean(parts[2].strip()), actual=checker.holds(state,FormulaParser.parse(expression));
                boolean match=actual==expected; if(match)pass++;else fail++;
                rows.add(String.join(",",csv(modelFile.getFileName().toString()),csv(state),csv(expression),
                            Boolean.toString(expected),Boolean.toString(actual),match?"PASS":"FAIL"));
            }
        }
        Files.write(target,rows);
        System.out.println("Detailed report: "+pass+" passed, "+fail+" failed in "+target);
        if(fail!=0)System.exit(1);
    }
}

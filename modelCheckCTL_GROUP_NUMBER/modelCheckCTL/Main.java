package modelCheckCTL;

import modelCheckCTL.controller.MainController;
import modelCheckCTL.model.*;
import modelCheckCTL.util.*;
import modelCheckCTL.view.MainWindow;
import javax.swing.SwingUtilities;
import java.nio.file.*;
import java.util.*;

/** Launch GUI or CLI. No third-party model-checking libraries. */
public final class Main {
    public static void main(String[] args) {
        if (args.length==0 || args[0].equals("--gui")) {
            SwingUtilities.invokeLater(() -> {
                MainWindow window = new MainWindow();
                new MainController(window);
                window.setVisible(true);
            });
            return;
        }
        try {
            int exit=switch(args[0]) {
                case "--check" -> check(args);
                case "--batch" -> batch(args);
                case "--suite" -> suite(args);
                case "--help", "-h" -> { usage(); yield 0; }
                default -> { usage(); yield 2; }
            };
            if(exit!=0) System.exit(exit);
        } catch (Exception e) { System.err.println("ERROR: " + e.getMessage());System.exit(2); }
    }
    private static int check(String[] args) throws Exception {
        if(args.length<4) { usage();return 2; }
        KripkeStructure model=KripkeParser.parseFile(Path.of(args[1]));
        String state=args[2], expression=String.join(" ",Arrays.copyOfRange(args,3,args.length));
        Formula f=FormulaParser.parse(expression);
        ModelChecker checker=new ModelChecker(model);
        boolean holds=checker.holds(state,f);
        System.out.println("Property '"+expression+"' "+(holds?"HOLDS":"DOES NOT HOLD")+" in state "+state);
        System.out.println("Satisfying states: "+checker.satisfyingStates(f));
        return 0;
    }
    private static int batch(String[] args) throws Exception {
        if(args.length!=3) {usage();return 2;}
        return batchFile(Path.of(args[1]), Path.of(args[2]));
    }
    private static int batchFile(Path modelPath, Path formulaPath) throws Exception {
        KripkeStructure model=KripkeParser.parseFile(modelPath);
        int pass=0, fail=0;
        int lineNumber=0;
        for (String line : Files.readAllLines(formulaPath)) {
            lineNumber++;
            line=line.replace("\uFEFF", "").trim();
            if(line.isEmpty() || line.startsWith("#")) continue;
            String[] parts=line.split(";",3);
            if(parts.length!=3) throw new IllegalArgumentException("Invalid test row at line "+lineNumber+" of "+formulaPath);
            String state=parts[0].trim(), expression=parts[1].trim();
            boolean expected=switch(parts[2].trim().toLowerCase(Locale.ROOT)) {
                case "true" -> true;
                case "false" -> false;
                default -> throw new IllegalArgumentException("Expected True/False at line "+lineNumber);
            };
            boolean actual=new ModelChecker(model).holds(state,FormulaParser.parse(expression));
            if(expected==actual) pass++; else {
                fail++; System.out.println("FAIL "+modelPath.getFileName()+" "+state+" "+expression+" expected="+expected+" actual="+actual);
            }
        }
        System.out.println(modelPath.getFileName()+": "+pass+" passed, "+fail+" failed");
        return fail==0?0:1;
    }
    private static int suite(String[] args) throws Exception {
        if(args.length!=2) {usage();return 2;}
        Path folder=Path.of(args[1]);int failed=0;
        for(int i=1;i<=7;i++) {
            Path model=folder.resolve("Model "+i+".txt");
            Path formulas=folder.resolve("Model "+i+" - Test Formulas.txt");
            if(Files.isRegularFile(model)&&Files.isRegularFile(formulas)) failed+=batchFile(model,formulas);
        }
        return failed==0?0:1;
    }
    private static void usage() {
        System.out.println("CTL Model Checker\n"+
            "  java -cp build modelCheckCTL.Main --gui\n"+
            "  java -cp build modelCheckCTL.Main --check MODEL.txt STATE 'CTL formula'\n"+
            "  java -cp build modelCheckCTL.Main --batch MODEL.txt 'Model - Test Formulas.txt'\n"+
            "  java -cp build modelCheckCTL.Main --suite 'tests/Test Files'");
    }
}

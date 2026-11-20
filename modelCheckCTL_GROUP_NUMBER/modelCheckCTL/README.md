# CS 5392 - Original CTL Model Checker

This is an **original Java/Swing implementation** of an explicit-state Computation Tree Logic (CTL) model checker. The provided C# project was **not used as a source-code template**. The model syntax, reference cases, and microwave figure were taken from the course files; algorithm design uses set-based fixed points.

## Prerequisites

- Java Development Kit (JDK) 17 or newer (tested with JDK 21).
- No external libraries or build system required.

On Windows (PowerShell), from inside the `modelCheckCTL` folder:

```powershell
New-Item -ItemType Directory -Force -Path build | Out-Null
$sources = @(Get-Item Main.java) + @(Get-ChildItem model,controller,view,util -Filter *.java)
javac -encoding UTF-8 -d build $sources.FullName
java -cp build modelCheckCTL.Main --gui
```

On Linux/macOS:

```bash
bash scripts/build.sh
java -cp build modelCheckCTL.Main --gui
bash scripts/test.sh
```

You can also run `java -jar modelCheckCTL.jar` if you have the supplied JAR.

## Application workflow

1. Click **Open model...** and select a Kripke model text file.
2. Select a **State ID** from the dropdown.
3. Enter a CTL property such as `AG(t1 -> AF c1)`.
4. Click **Check formula** to see whether the property holds in that state and the list of all satisfying states.
5. For a malformed model or expression, a dialog displays the error description; malformed model errors include the line number, and CTL syntax errors include the column.

## Command-line operation

```bash
java -cp build modelCheckCTL.Main --check 'tests/fixtures/Model 1.txt' s1 'EXq'
java -cp build modelCheckCTL.Main --batch 'tests/fixtures/Model 4.txt' 'tests/fixtures/Model 4 - Test Formulas.txt'
java -cp build modelCheckCTL.Main --suite tests/fixtures
java -cp build modelCheckCTL.Main --batch tests/microwave/microwave.txt tests/microwave/microwave-formulas.txt
```

### Kripke input format

The input has **three semicolon-terminated sections**, each with comma-separated entries: (1) state IDs, (2) named directed transitions, and (3) the propositions true in each state.

```text
s1, s2;
t1 : s1 - s2,
t2 : s2 - s2;
s1 : p,
s2 : q;
```

In the project description, a state name is an identifier, each edge is a source/destination pair, and atomic propositions are case sensitive. The provided **Model 4** test uses names such as `c1` and numeric atoms `0`, `1`, `2`; this implementation intentionally supports these.

A state with empty propositions is written `s3 : ,`. A model may have a terminal state (no outgoing transition), because several course test fixtures contain one. This input format does **not** declare initial states; the checker instead verifies the selected state, or enumerates all satisfying states.

### CTL formula syntax

| Operator | Example | Meaning |
| --- | --- | --- |
| `not` | `not p` | Not p |
| `and` | `p and q` | Both hold |
| `or` | `p or q` | At least one holds |
| `->` | `p -> q` | If p, then q |
| `EX` | `EXp` | Some immediate successor holds p |
| `AX` | `AXp` | Every immediate successor holds p |
| `EF` | `EFp` | Along some path, eventually p |
| `AF` | `AFp` | Along all paths, eventually p |
| `EG` | `EGp` | Along some infinite path, always p |
| `AG` | `AGp` | Along all paths, always p |
| `E(... U ...)` | `E(pUq)` | Some path holds p until q |
| `A(... U ...)` | `A(pUq)` | All paths hold p until q |

`E[p U q]` and `A[p U q]` are also accepted. Parentheses group subexpressions. Operators have conventional precedence: unary CTL and `not` bind tightest, then `and`, then `or`, then right-associative `->`.

Note: uppercase `A`, `G`, `F`, `X`, and `U` are treated as reserved operator characters; avoid them as standalone atomic proposition names. The reference test suite specifically expects malformed forms such as `Gp`, `AGAp`, and `EFGr` to be rejected.

### CTL checking algorithm

Let `Sat(f)` be the set of states where CTL formula `f` holds, and `PreExists(X)` the states with at least one immediate successor in set `X`.

- `Sat(p)`: states whose label contains proposition p.
- `Sat(not f) = S \\ Sat(f)`.
- `Sat(f and g) = Sat(f) intersection Sat(g)`.
- `Sat(EX f) = PreExists(Sat(f))`.
- `Sat(EF f)`: least fixed point of `X = Sat(f) union PreExists(X)`.
- `Sat(AF f)`: least fixed point of `X = Sat(f) union PreAll(X)`.
- `Sat(E(f U g))`: least fixed point of `X = Sat(g) union (Sat(f) intersection PreExists(X))`.
- `Sat(A(f U g))`: least fixed point of `X = Sat(g) union (Sat(f) intersection PreAll(X))`.
- `Sat(EG f)`: greatest fixed point of `X = Sat(f) intersection PreExists(X)`.
- `Sat(AG f)`: greatest fixed point retaining states with `f` and all successors retaining it.

The checker repeatedly updates a finite set until no states change. There are at most `|S|` strict additions/removals for each fixed point. `ModelChecker.java` is the independent implementation.

**Terminal-state convention:** In the supplied Model 5 tests, `AXp` is false for terminal states, even when `p` is true at the state. The project therefore chooses a **non-vacuous** `PreAll`: at least one successor is required. `AF`/`AU` use this to avoid treating an unfulfilled eventuality on a terminal path as true. `AG` allows a terminal state satisfying `p` to satisfy `AGp` on its finite maximal path. `EG` requires a successor to continue the witness path. Textbook definitions often require all states to have successors; discuss this explicit choice during the presentation.

## Architecture

- `modelCheckCTL.model.State`: one state and its atomic propositions.
- `modelCheckCTL.model.Transition`: directed named arc.
- `modelCheckCTL.model.KripkeStructure`: state labels, edges, successors.
- `modelCheckCTL.model.Formula`: typed abstract syntax tree (AST).
- `modelCheckCTL.model.ModelChecker`: bottom-up CTL evaluator with fixed points.
- `modelCheckCTL.util.KripkeParser`: model file parser and validator.
- `modelCheckCTL.util.FormulaParser`: tokenization, precedence, and CTL grammar.
- `modelCheckCTL.view.MainWindow`: Swing user interface.
- `modelCheckCTL.controller.MainController`: connects GUI actions to parsers and evaluator.
- `modelCheckCTL.Main`: application/command-line entry point.

The UML diagram is in `docs/UMLClassDiagram.pdf`.

## Verification status

On the current test run, all 199 supplied formula expectations from Models 1-7 matched. The microwave model was independently transcribed from the figure in `mchPeled.zip` (chapter 4, Figure 4.3), with 13 successful formula checks. All eight broken models generated parser errors; seven invalid formulas were rejected, and a nonexistent state ID was rejected. See `docs/AcceptanceTestCases.pdf`, `docs/AcceptanceTestExecution.pdf`, and `docs/screenshots/`. Each of the 212 formula checks is also individually recorded in `docs/DetailedTestResults.csv` with expected and actual values.

**Verification limitations:** These are local automated checks and Swing screenshots captured on a Linux virtual desktop. Group members should also manually test the final ZIP on the computer they will use for presentation. The reference archive's C# code was not copied.

## Presentation walkthrough

1. Explain `M=(S,R,L)`: states, successor relation, labels; this input format does not explicitly provide initial states.
2. Explain `Sat(f)` and evaluate `EXq` at `s1` in Model 1.
3. Demonstrate why `AXq` at `s1` is false (not every immediate successor has `q`).
4. Demonstrate critical section Model 4: `AG(t1 -> AF c1)` at `s1` evaluates true.
5. Demonstrate microwave: `AG(Start -> AF Heat)` at `s1` evaluates false, due to a cycle with Start true but Heat remaining false.
6. Show an invalid model, e.g., `Broken Model 3.txt`, producing a line-specific error.
7. Explain fixed-point iteration and how the `model`, `view`, and `controller` code is separated.

## Integrity and submission

- The test fixture text/figures are provided course inputs; their presence is not a claim of authorship.
- The microwave text fixture is a new transcription of the scanned figure; it is not taken from example C# source.
- Add all group members' actual AI prompts to `docs/Prompts.md`.
- Rename the parent ZIP to `modelCheckCTL<YourGroupNumber>.zip` before submitting. Do not submit with the placeholder group number.

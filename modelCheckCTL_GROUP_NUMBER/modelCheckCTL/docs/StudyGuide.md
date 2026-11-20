# Presentation Study Guide: CTL Models and Notation

## Kripke structure

A Kripke model is often written `M = (S, S0, R, L)` (some textbooks omit `S0`). Here `S` is the finite state set, `S0` is the initial-state set, `R` is the directed transition relation, and `L(s)` is the set of atomic propositions true at state `s`. The provided model input does not specify `S0`; therefore the user selects a state to check.

Example: Model 1 includes transition `t1 : s1 - s2`, so `(s1,s2)` belongs to `R`. It labels `s1` with `{p, q}` and `s2` with `{q, t, r}`. A proposition is a Boolean fact such as `q`; a state ID like `s1` is not itself a proposition.

## Operators

- `E`: there **exists** a path, also called existential path quantification.
- `A`: for **all** paths, also called universal path quantification.
- `X`: in the ne**x**t state.
- `F`: at some point in the **future** (eventually).
- `G`: **globally**, i.e., always.
- `U`: **until**: the left statement continues to hold until the right statement becomes true.
- `not`: negation; `and`: conjunction; `or`: disjunction; `->`: implication.
- `M, s |= f`: formula `f` is true at state `s` in model `M`.
- `Sat(f)`: all states where `f` holds.
- `mu` / least fixed point: repeatedly **add** states until stable.
- `nu` / greatest fixed point: repeatedly **remove** states until stable.

## Two worked examples

**Model 1 - EXq at s1:** `s1` has edges to `s2` and `s3`. `q` is true in `s2`, so *there exists* a next state with `q`. Result: true.

**Model 1 - AXq at s1:** `q` is false in `s3`. Because not *every* immediate successor has `q`, result: false.

## Critical-section example (Model 4)

The states label a concurrent-process model with propositions `n1`, `t1`, `c1`, `n2`, `t2`, `c2`, and a numeric label. Think of `n1` as noncritical, `t1` as trying, and `c1` as critical for process 1. The supplied property `AG(t1 -> AF c1)` means: **from every reachable state and on all continuations, whenever process 1 is trying, it must eventually reach its critical section on every possible continuation**. The supplied expected result is true at all nine states of Model 4.

It checks a form of progress/liveness, not mutual exclusion. A separate mutual-exclusion property could be `AG not (c1 and c2)`, meaning both processes never occupy their critical sections together.

## Microwave example (mchPeled.zip / Figure 4.3)

A seven-state model uses propositions `Start`, `Close`, `Heat`, and `Error`. The scanned chapter tests `AG(Start -> AF Heat)` (equivalent to `not EF(Start and EG not Heat)`). It is false in initial state 1 because a cycle can maintain Start without ever reaching Heat (states 2 and 5). The checker uses the transcribed model `tests/microwave/microwave.txt` and labels states as `s1` through `s7`.

## Algorithm explanation that each student should practice

For `EFp`: collect states with `p`, then add predecessor states that have at least one transition into the collected set. Continue until a pass adds no states. For `AFp`: collect states with `p`, then add predecessor states **only when every possible successor** is already collected, with special handling of terminal states. Finite graphs guarantee convergence.

## Vocabulary

- **Parser**: turns input text into a structured model or a formula tree.
- **Lexer/tokenizer**: breaks a formula such as `EXq and p` into tokens such as `EX`, `q`, `and`, `p`.
- **Abstract syntax tree (AST)**: a tree encoding the operators and operands of a formula.
- **Successor**: next state reachable along one directed edge.
- **Predecessor**: a state with an edge pointing into another state.
- **Fixed point**: a set that no longer changes after another application of the update rule.
- **MVC**: Model-View-Controller, a way of separating logic, UI, and event handling.
- **UML**: Unified Modeling Language, a standardized diagram notation for software design.

## Individual oral-check questions

1. Explain `EXq` and `AXq` at `s1` of Model 1, using the edges.
2. What is the difference between `EF` and `AF`?
3. How do `EU` and `AU` differ?
4. Why does a fixed-point algorithm eventually stop on a finite graph?
5. What happens if a model references an undefined state?
6. Why does Model 4 pass `AG(t1 -> AF c1)`?
7. Why does the microwave model fail `AG(Start -> AF Heat)`?
8. Which class parses formulas, which stores graph states, and which checks CTL formulas?
9. Why did we keep all example C# source outside our implementation?

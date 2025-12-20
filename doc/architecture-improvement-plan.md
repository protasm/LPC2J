# LPC2J Architecture Improvement Plan

This plan breaks down the architectural improvements suggested in `architecture-review.md` into concrete, ordered steps. Each step includes its objective, key tasks, and dependencies so the work can proceed incrementally without large rewrites.

## 1. Establish a structured compilation pipeline and diagnostics
- **Objective:** Separate user-facing output from the core API and expose phase-by-phase results (scan → parse → analyze → codegen) with machine-readable diagnostics.
- **Key tasks:**
  - Introduce a pipeline orchestrator that runs phases sequentially and records successes/failures without printing to stdout.
  - Define a reusable diagnostic/problem type that captures the phase, message, and underlying exception or location when available.
  - Keep existing entry points delegating to the pipeline for backward compatibility while steering new callers to the structured API.
- **Dependencies:** None; foundational.

## 2. Upgrade source position tracking and token metadata
- **Objective:** Provide accurate file/line/column/span information so diagnostics remain precise after preprocessing.
- **Key tasks:**
  - Add span objects that carry filename, start/end line, column, and character offsets.
  - Extend `Token` (and related cursor helpers) to embed spans instead of line-only positions.
  - Ensure scanners populate spans and propagate them through parsing.
- **Dependencies:** Step 1 for structured diagnostics to consume spans.

## 3. Preserve source mapping across preprocessing
- **Objective:** Avoid losing token boundaries and location mapping during macro/include expansion.
- **Key tasks:**
  - Introduce a preprocessor mapping that records how expanded characters map back to original files.
  - Adapt the scanner to accept either raw source or preprocessor tokens with mapping info.
  - Emit diagnostics using mapped spans so include and macro errors point to original sources.
- **Dependencies:** Step 2 to represent spans; builds on structured pipeline from Step 1.

## 4. Simplify scanner responsibilities and defer language semantics
- **Objective:** Decouple lexical analysis from type knowledge and improve recovery.
- **Key tasks:**
  - Remove hardcoded type/keyword classification from the scanner; emit identifier tokens with categories determined later.
  - Add basic error recovery for unterminated tokens and malformed literals while preserving spans.
  - Keep lexer output stable for later semantic classification.
- **Dependencies:** Steps 2–3 to rely on spans and mapping.

## 5. Introduce a semantic analysis pass with symbol tables
- **Objective:** Separate parsing from semantic validation and produce a typed, validated AST.
- **Key tasks:**
  - Define symbol table structures (scopes, declarations, fields/efuns) and a pass manager to run semantic checks after parsing.
  - Move type keyword recognition and contextual type resolution out of the scanner and into this analyzer.
  - Emit typed AST annotations or a separate semantic model used by later stages.
- **Dependencies:** Steps 1–4; enables downstream IR work.

## 6. Modernize AST traversal (visitor-lite / sealed hierarchy)
- **Objective:** Reduce boilerplate for new passes and improve exhaustiveness.
- **Key tasks:**
  - Refactor AST nodes toward sealed interfaces/classes where possible.
  - Replace per-node visitor overloads with a generic visitor or pattern matching walker usable across passes.
  - Update existing passes (type propagation, printing, codegen) to use the new traversal utilities.
- **Dependencies:** Step 5 to align traversal with the semantic model.

## 7. Define runtime type model and typed IR
- **Objective:** Create an intermediate representation that makes semantics explicit before code generation.
- **Key tasks:**
  - Specify runtime value shapes (objects, arrays, efuns, truthiness) and how they map to JVM constructs.
  - Design a small, typed IR capturing control flow, calls (including efuns/dynamic dispatch), and coercions.
  - Provide lowering rules from the typed AST/semantic model to the IR.
- **Dependencies:** Step 5 (typed semantics) and Step 6 (traversal helpers).

## 8. Refactor code generation to consume the typed IR
- **Objective:** Isolate bytecode emission from semantic decisions.
- **Key tasks:**
  - Implement IR-to-ASM emission that assumes prior validation.
  - Remove semantic conditionals from the emitter (e.g., ad hoc coercions, reflection fallbacks) by encoding them in the IR.
  - Add targeted tests for IR lowering and emission paths.
- **Dependencies:** Step 7 IR availability.

## 9. Clarify runtime boundaries and efun contracts
- **Objective:** Replace global efun state with explicit runtime contexts and richer type information.
- **Key tasks:**
  - Introduce a runtime context object that owns efun registries, object lifecycle, and include resolution.
  - Define efun signatures (parameter and return types) so the analyzer can validate calls and support overloading.
  - Adjust codegen/runtime glue to use the context instead of static registries.
- **Dependencies:** Steps 5 and 7 for typing and runtime model.

## 10. Decouple the console from the core runtime
- **Objective:** Let the console consume the compiler/runtime without establishing global state.
- **Key tasks:**
  - Refactor console setup to build a runtime context instance rather than modifying static globals.
  - Route REPL compilation/execution through the structured pipeline and runtime context.
  - Preserve existing console behavior while enabling embedders to supply their own contexts.
- **Dependencies:** Step 9 runtime context.

## 11. Regression testing and documentation updates
- **Objective:** Keep the evolution verifiable and well-documented.
- **Key tasks:**
  - Add incremental tests for each new phase (scanner spans, preprocessor mapping, semantic checks, IR lowering, codegen).
  - Update developer docs to describe the new pipeline, passes, and runtime boundaries.
  - Provide migration notes for any public API changes (e.g., `LPC2J` entry points, efun registration).
- **Dependencies:** Runs throughout; finalize after core refactors.

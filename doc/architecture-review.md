# LPC2J Architectural Review

## Executive Summary
- **Phases blurred together:** Preprocessing, scanning, parsing, minimal “type inference,” and bytecode emission are chained directly without a stable intermediate representation (IR) or a true semantic analysis pass, making it hard to enforce LPC semantics or evolve the compiler. The parser mutates the AST and symbol metadata in place and the compiler consumes it immediately, so there is no separation between front end, middle end, and back end responsibilities.【F:src/io/github/protasm/lpc2j/LPC2J.java†L8-L61】【F:src/io/github/protasm/lpc2j/compiler/Compiler.java†L32-L86】
- **Visitor pattern is tightly coupled and underpowered:** Each AST node hardcodes three distinct visitor types (compiler, type inference, printer), preventing reuse of traversals and forcing new visitors to edit every node. The “type inference” visitor mostly propagates contextual hints and mutates symbol types, but it is not a full constraint solver or checker, so the pattern adds boilerplate without delivering strong guarantees.【F:src/io/github/protasm/lpc2j/parser/ast/ASTNode.java†L5-L31】【F:src/io/github/protasm/lpc2j/parser/ast/visitor/TypeInferenceVisitor.java†L1-L143】
- **Semantic concerns leak into code generation:** The ASM emitter performs name resolution, dynamic invocation, boxing, and ad hoc type coercion while walking the AST, effectively combining semantic analysis and codegen. This makes it difficult to validate programs independently of the backend and creates brittleness as LPC features expand.【F:src/io/github/protasm/lpc2j/compiler/Compiler.java†L88-L209】【F:src/io/github/protasm/lpc2j/compiler/Compiler.java†L211-L375】
- **Console/runtime entanglement:** The console registers efuns globally and loads compiled classes reflectively, coupling the user-facing REPL to core runtime choices. There is no explicit runtime abstraction or object model boundary, so CLI concerns and execution semantics share state (e.g., the static `EfunRegistry`).【F:src/io/github/protasm/lpc2j/console/LPCConsole.java†L31-L125】【F:src/io/github/protasm/lpc2j/efun/EfunRegistry.java†L5-L27】

## Package-by-Package Review

### `io.github.protasm.lpc2j` (root)
- **Role:** Thin facade that chains scan → parse → compile with optional parser options, printing errors to stdout and returning null on failure.【F:src/io/github/protasm/lpc2j/LPC2J.java†L8-L61】
- **Patterns:** Procedural orchestration with exception trapping inside each stage.
- **Assessment:** Convenient for a demo, but it cements a monolithic pipeline and mixes UI concerns (printing) into the core API. A composable pipeline (preprocess → lex → parse → analyze → codegen) would allow tools and tests to reuse individual phases and surface structured diagnostics.

### `token` and `sourcepos`
- **Role:** Token representation plus basic source-position helpers (`CharCursor`, `LineMap`), but tokens only store a line number and are not linked to richer position objects.【F:src/io/github/protasm/lpc2j/token/Token.java†L1-L31】
- **Patterns:** Simple record-based tokens with ad hoc position tracking.
- **Assessment:** Lacking columns/ranges and file identity undermines diagnostics. If preprocessing stays as a text-to-text step, lexing should emit tokens annotated with full spans in the post-include coordinate space; otherwise errors will drift and later phases cannot reconstitute accurate locations.

### `preproc`
- **Role:** A standalone macro preprocessor that expands includes and macros, strips comments, and returns expanded source text before lexing.【F:src/io/github/protasm/lpc2j/preproc/Preprocessor.java†L33-L125】【F:src/io/github/protasm/lpc2j/preproc/Preprocessor.java†L136-L212】
- **Patterns:** Textual expansion with a minimal hideset, no token stream, and a rejecting include resolver by default.
- **Assessment:** Text-level expansion discards original token boundaries and source mapping, which complicates later diagnostics and macro-aware parsing. A token-level preprocessor (or at least a mapping from expanded characters back to original files) would better support precise error reporting and future macro semantics.

### `scanner`
- **Role:** Preprocesses, then lexes into a mutable `TokenList`; also recognizes LPC types and reserved words.【F:src/io/github/protasm/lpc2j/scanner/Scanner.java†L58-L161】【F:src/io/github/protasm/lpc2j/scanner/Scanner.java†L163-L231】
- **Patterns:** Single-pass lexer driven by a `ScannableSource` and global maps for keywords/types.
- **Assessment:** Tightly coupled to the preprocessor (assumes pre-expanded text) and emits tokens without richer source spans or error recovery. The scanner also encodes type keywords directly, pushing language knowledge into lexing rather than semantic classification.

### `parser` (core, `parselet`, `ast`, `type`)
- **Role:** Two-pass declaration/definition parser plus a third pass for rudimentary type propagation. Expression parsing is Pratt-style via `parselet` helpers; statements and declarations are hand-coded. AST nodes carry symbols and optional type info; locals are managed via a manual scope stack.【F:src/io/github/protasm/lpc2j/parser/Parser.java†L25-L161】【F:src/io/github/protasm/lpc2j/parser/Parser.java†L163-L273】【F:src/io/github/protasm/lpc2j/parser/Locals.java†L5-L63】
- **Patterns:** Multi-pass mutation of a shared AST, tightly-coupled visitors, and contextual “type inference” that sets symbol types based on usage rather than constraints.【F:src/io/github/protasm/lpc2j/parser/ast/visitor/TypeInferenceVisitor.java†L1-L143】
- **Assessment:** Responsibilities are blurred: parsing, symbol binding, and type checking are interleaved on a single mutable structure. The Visitor pattern is used in a bespoke way (every node declares three concrete visitor types), which limits extensibility and makes adding analyses expensive. The type propagation pass lacks constraints, overload resolution, and flow-sensitivity, so the AST can be consumed by the backend without guarantees. The presence of `.old` parselets hints at incomplete or abandoned designs, suggesting the parsing layer is unstable.

### `compiler`
- **Role:** Direct ASM emitter that walks the AST to produce a Java class, handling locals, control flow, boxing/unboxing, efun calls, and reflective method invocation for dynamic calls.【F:src/io/github/protasm/lpc2j/compiler/Compiler.java†L32-L209】【F:src/io/github/protasm/lpc2j/compiler/Compiler.java†L211-L375】
- **Patterns:** Visitor-style codegen with embedded semantic decisions (e.g., null checks for efuns, coercion logic, dynamic invocation via reflection).
- **Assessment:** Codegen doubles as semantic enforcement and runtime bridging. Without an IR or verified symbol table, the compiler must defensively handle missing types and dynamic dispatch, which will not scale to richer LPC semantics (inheritance resolution, mixins, efun overloading). ASM emission is hand-written per AST node, so changing the language requires editing the emitter everywhere rather than transforming through a normalized IR.

### `runtime`
- **Role:** Minimal truthiness helper only.【F:src/io/github/protasm/lpc2j/runtime/Truth.java†L5-L46】
- **Assessment:** There is no articulated runtime object model or value representation; semantics leak into the compiler and efun interfaces instead of being centralized here.

### `efun`
- **Role:** Interface for efuns plus a global registry used by the compiler and console.【F:src/io/github/protasm/lpc2j/efun/Efun.java†L1-L33】【F:src/io/github/protasm/lpc2j/efun/EfunRegistry.java†L5-L27】
- **Patterns:** Static global registry; efuns expose `Symbol` and arity but no type contracts beyond integer counts.
- **Assessment:** Global state complicates testing and multiple runtimes. Efuns are typed only by arity and optional `Symbol` metadata, so the compiler cannot statically validate calls or perform overloading; everything degrades to runtime checks or reflection.

### `console` (including `fs`, `cmd`, `efuns`)
- **Role:** Interactive REPL with virtual FS, include resolution, command dispatch, efun registration, compilation, dynamic class loading, and object inspection.【F:src/io/github/protasm/lpc2j/console/LPCConsole.java†L31-L202】
- **Patterns:** Stateful REPL with imperative command objects; uses reflection to load compiled classes and populate a global object map.
- **Assessment:** The console is positioned as a consumer of the core, but it currently establishes global runtime state (efun registrations, include resolver) that the compiler implicitly relies on. Execution concerns (class loading, object lifetime) are intertwined with the CLI. A more explicit runtime context would let non-console hosts embed LPC2J without inheriting console assumptions.

## Alternative Architectural Directions

1) **Phase-separated compiler with a validated AST and semantic table**
   - **Approach:** Keep the Pratt parser but introduce a dedicated semantic analysis pass that builds symbol tables, resolves inheritance/fields/efuns, and emits a typed AST or IR. Separate error reporting from codegen, and move type keywords out of the lexer into the analyzer.
   - **Pros:** Clear front/middle/back boundaries; static validation before codegen; easier to add flow-sensitive checks and richer diagnostics.
   - **Cons:** Requires additional data structures (scopes, symbol table, type system) and a rewrite of codegen to consume the validated IR.

2) **Typed IR + transform-driven backend**
   - **Approach:** Lower parsed AST into a small, typed IR (e.g., three-address form or a JVM-oriented SSA-like subset) that normalizes control flow and calls. Backend becomes a pure IR-to-bytecode emitter; efun calls and dynamic dispatch are reified as explicit IR nodes with resolved types.
   - **Pros:** Decouples language evolution from ASM details; enables optimization and multiple backends (interpreter, bytecode, ahead-of-time). Easier to unit test passes independently.
   - **Cons:** More up-front design; needs a well-defined runtime type model and lowering rules.

3) **Visitor-lite with pattern matching and pass manager**
   - **Approach:** Replace per-class visitor overloads with a generic visitor interface or pattern-matching walkers (e.g., using sealed interfaces and switch expressions) plus a pass manager that runs analyses in order. Keep ASTs immutable where possible.
   - **Pros:** Adding new analyses does not require touching every node; sealed hierarchies improve exhaustiveness; pass manager clarifies phase ordering.
   - **Cons:** Requires refactoring AST definitions and existing passes; may introduce more allocation unless carefully tuned.

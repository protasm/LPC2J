# Post-Plan Architectural Review

## How it turned out
- The structured `CompilationPipeline` now runs scan → parse → analyze → lower → compile while collecting `CompilationProblem`s per phase instead of printing directly, giving tools access to tokens, ASTs, semantic models, typed IR, and bytecode in one place.
- Source tracking is precise end-to-end: tokens carry `SourceSpan`s, and the preprocessor preserves mappings back to original files so diagnostics can cite real line/column information even after macro/include expansion.
- A real middle end emerged: semantic analysis resolves symbol types, validates efun calls, infers return signatures, and lowers into a typed JVM-oriented IR that code generation consumes without redoing semantic work.
- Runtime concerns are explicitly owned by `RuntimeContext`, which carries efun registration and include resolution; the console drives compilation through that context rather than via globals, making embedding predictable.

## What went well
- **Phase separation** – The pipeline exposes each stage’s artifacts, making it easier to debug or reuse individual phases.
- **Location fidelity** – Span-aware tokens plus preprocessor mapping keep diagnostics aligned with original source text.
- **Typed IR contract** – Lowering normalizes control flow and call shapes so the ASM emitter can focus purely on bytecode.
- **Runtime clarity** – Instance-scoped efun registries and include resolvers mean hosts can supply their own runtime configuration without shared globals.

## What still needs work
- **Richer diagnostics surfaces** – `CompilationProblem` currently exposes only stage/line/throwable; threading full `SourceSpan` data through results would sharpen user-facing errors.
- **Deeper semantic checking** – The legacy type-hinting visitor has been replaced, but further constraint solving (e.g., flow-sensitive narrowing, better mixed/null handling) would strengthen guarantees.
- **Preprocessor/scanner recovery** – Error recovery remains minimal; leveraging the existing span mapping for recoverable lex/preprocess errors would improve resilience.
- **Console/runtime boundary** – The console still performs reflective loading and invocation directly; a higher-level runtime object model could enforce safety and lifecycle hooks consistently.

# Regression testing and the phase-separated pipeline

## Pipeline and runtime boundaries
- The compiler is now organized around an explicit pipeline: preprocess → scan → parse → analyze → lower (typed IR) → compile. The structured runner is `io.github.protasm.lpc2j.pipeline.CompilationPipeline`, which returns a `CompilationResult` containing intermediate artifacts plus any `CompilationProblem`s per stage.
- Runtime concerns (efuns, include resolution, object registry) live behind `io.github.protasm.lpc2j.runtime.RuntimeContext`. Host applications should instantiate a context, register efuns on it, and hand it to the pipeline so global singletons are avoided.
- Entry points in `LPC2J` delegate to the pipeline; callers that need diagnostics or custom class names should prefer `CompilationPipeline#run` so they can supply a `sourcePath`/`sourceName` pair instead of relying on the legacy `compile(String)` helpers.

## Regression coverage (Step 11)
- A lightweight harness at `src/test/java/io/github/protasm/lpc2j/testing/PipelineRegressionTests.java` exercises each phase:
  - Preprocessor mapping through includes/macros
  - Scanner span propagation
  - Semantic analysis error reporting
  - Typed IR lowering of arithmetic returns
  - End-to-end code generation and reflective invocation
- Run the suite with `./test`, which compiles sources (including tests) into `out/` and executes the assertions with `-ea` enabled.

## Migration notes
- Prefer `CompilationPipeline` (or `LPC2J.compileWithDiagnostics`) for new tooling so diagnostics and intermediate artifacts are available without printing to stdout.
- Register efuns on a `RuntimeContext` instance rather than using static registries; the console now builds and passes its own context.
- When compiling from files, provide a stable `sourceName` to `CompilationPipeline#run` so the generated class has a valid JVM internal name instead of relying on raw filesystem paths.

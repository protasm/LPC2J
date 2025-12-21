# AGENTS (start here)

- Use this file as the quick-start guide before making any changes. It summarizes how LPC2J is shaped and where to look first.

## What this repo is
- LPC2J is an LPC90-compatible compiler that turns LPC source into JVM classfiles. The core flow is preprocess → scan → parse → semantic analysis → IR lowering → ASM bytecode emission.
- The public, one-shot entry points live in `io.github.protasm.lpc2j.LPC2J` (static helpers) and `io.github.protasm.lpc2j.pipeline.CompilationPipeline` (structured result collection). The interactive REPL is `io.github.protasm.lpc2j.console.LPCConsole`.

## Where things live
- `src/io/github/protasm/lpc2j/preproc`, `scanner`, `parser`, `semantic`, `ir`, and `compiler` mirror the compilation stages. `runtime` holds minimal helpers; `efun` defines the efun registry; `console` contains the REPL and its commands/fs helpers. Tests live under `src/test/java/io/github/protasm/lpc2j/testing`.
- `doc/` contains architecture notes, plans, and design reviews—skim here for background before significant refactors.
- `libs/` carries third-party jars referenced by the simple shell scripts. `comp` builds all sources into `out`; `test` compiles then runs `PipelineRegressionTests` with assertions enabled.

## How to build and test quickly
- `./comp` — compiles Java sources against `libs/*` into `out`.
- `./test` — cleans `out`, rebuilds, then runs the regression test harness (`PipelineRegressionTests`).

## Working conventions
- Prefer `ParserOptions.defaults()` unless tests need specific toggles; thread `RuntimeContext` through when includes or efuns matter (console does this already).
- Avoid folding semantic decisions into codegen unless necessary; keep changes phase-appropriate (scanner for tokens, parser for structure, semantic for symbols/types, IR for lowering, compiler for emission).
- When touching language behavior, look for matching coverage in `src/test/java/.../PipelineRegressionTests` and add focused cases there.
- Respect the environment guideline to use `rg` for search instead of recursive `ls`/`grep`.

## Required actions
- Read AGENTS.md before editing any files.
- Update AGENTS.md with every PR drafted.

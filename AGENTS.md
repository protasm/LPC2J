# AGENTS.md — LPC2J

This repository contains **LPC2J**, a Java-based compiler / transpiler and runtime
for the LPC (MUD) programming language, along with a console-based frontend.

Automated agents (including Codex) are welcome to assist, but must respect the
project’s architectural intent and constraints.

---

## Project Intent

LPC2J is a **language toolchain**, not a typical application.

Primary goals:
- Faithful representation of LPC semantics
- Clarity and correctness over cleverness
- Incremental evolution rather than large rewrites
- Human readability of compiler structure

Performance optimizations are secondary to correctness and maintainability unless
explicitly requested.

---

## High-Level Structure

- `io.github.protasm.lpc2j.*`
  - Core compiler, runtime, and language infrastructure
- `io.github.protasm.lpc2j.console.*`
  - Console / CLI frontend for invoking the compiler and interacting with LPC2J
- `io.github.protasm.lpc2j.test.*`
  - Sample LPC files for testing

The console is a **consumer of the core**, not the core itself.

---

## Guidelines for Automated Changes

Agents SHOULD:
- Think in terms of compiler phases (scan, parse, analyze, generate, runtime)
- Preserve existing abstractions unless they are clearly accidental
- Prefer small, localized changes over cross-cutting refactors
- Explain *why* a change improves correctness, extensibility, or clarity
- Flag architectural concerns even if no code change is proposed

Agents SHOULD NOT:
- Introduce frameworks (Spring, Lombok, etc.) without explicit request
- Optimize prematurely or micro-optimize without evidence
- Convert the project to Maven/Gradle unless explicitly asked

---

## Dependency Policy

- External dependencies are intentionally minimal
- ASM is used deliberately for bytecode-related work
- New dependencies should be proposed with clear justification

---

## Error Handling & Diagnostics

Error handling is considered part of the compiler’s user interface.

Agents should:
- Prefer explicit, descriptive errors
- Avoid swallowing exceptions
- Preserve contextual information useful for debugging LPC code

---

## Testing Philosophy

Testing may be lightweight or incomplete.

Agents may:
- Suggest test strategies or harnesses
- Add tests when making nontrivial behavioral changes

Agents should not:
- Block changes solely due to missing tests unless correctness is at risk

---

## How to Help Best

When proposing changes, agents are encouraged to:
1. Briefly describe the problem or limitation being addressed
2. Explain why the change aligns with LPC2J’s goals
3. Propose the *smallest viable improvement*

Architectural commentary is welcome even when no immediate code change is made.

---

## When in Doubt

If uncertain about:
- Architectural direction
- Language semantics
- Intended scope

→ Ask or explain assumptions before making sweeping changes.


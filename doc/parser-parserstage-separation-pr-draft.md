# PR Draft: Move semantic checks out of parser

## Summary
This draft PR addresses the parser-stage violations previously identified and relocates semantic logic to appropriate stages of the compiler pipeline. The changes aim to keep parsing limited to grammar recognition and AST construction while deferring symbol binding, inheritance validation, and ordering rules to semantic analysis.

## Issues Addressed
1. **Inheritance validation in the parser**
   - Current behavior: Parser rejects multiple `inherit` statements and enforces ordering before member declarations.
   - Plan: Parse and record inherit directives without enforcing single-inheritance or ordering. Move enforcement to semantic analysis.

2. **Cross-pass field/method definition validation**
   - Current behavior: Parser throws errors when definitions lack prior declarations.
   - Plan: During parsing, always create AST entries for declarations/definitions and preserve occurrence order. Defer “unrecognized” checks to semantic analysis.

3. **Identifier resolution during expression parsing**
   - Current behavior: Prefix identifier parselet resolves locals, fields, methods, and efuns immediately, throwing on misses.
   - Plan: Replace eager resolution with AST nodes that carry identifier text (and perhaps arity) only. Semantic/binding phase will resolve to locals/fields/methods/efuns.

4. **Scope/slot assignment and duplicate-local checks in parser**
   - Current behavior: Parser assigns local slots, tracks scope depth, reserves slot 0, and rejects duplicates/self-references.
   - Plan: Parser only notes local declarations and scope boundaries. Semantic or lowering stage assigns slots and enforces duplicate/self-reference errors.

## Proposed Changes
- **Parser (`Parser.java`)**
  - Remove single-inheritance and ordering enforcement; simply collect inherit directives and attach the parent name to the AST object.
  - Record declarations and definitions without cross-validation; keep per-occurrence lists for later analysis.
  - Adjust local declaration handling to build AST locals without performing duplicate checks or slot/depth assignment.

- **Parselets (`PrefixIdentifier.java` and related)**
  - Emit identifier-based AST nodes instead of resolving to locals/fields/methods/efuns. Introduce placeholder nodes if needed (e.g., `ASTExprUnresolvedIdentifier`, `ASTExprUnresolvedCall`).

- **AST adjustments**
  - Ensure AST nodes preserve source order and necessary metadata (identifier name, argument count, source locations) without embedding resolution results or slot numbers.
  - Keep `ASTMapNode` occurrence tracking intact for downstream semantic checks.

- **Semantic analysis layer**
  - Introduce or extend a semantic pass to:
    - Enforce single-inheritance and pre-declaration ordering rules.
    - Verify that field/method definitions match declarations and handle overloading/duplicates per language rules.
    - Resolve identifiers to locals/fields/methods/efuns and report missing symbols.
    - Assign local slots, scope depths, and prevent duplicate locals/self-referential initializers.

## Testing Plan
- Update or add regression tests in `PipelineRegressionTests` to cover:
  - Multiple `inherit` statements and late `inherit` declarations reported during semantic analysis.
  - Missing field/method declarations detected after parsing.
  - Unknown identifiers and efun resolution handled in semantic phase.
  - Duplicate locals and self-referential initializers flagged by semantic analysis.
- Run `./test` to validate end-to-end behavior.

## Notes
- No code changes are implemented in this draft; it captures the intended refactor scope to separate parsing from semantic responsibilities.

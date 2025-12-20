# Nightmare IV mudlib compatibility plan for LPC2J

## Mudlib observations
- The mudlib expects layered headers under `secure/include`, `include`, and `lib/include`, with path macros such as `LIB_*` pointing to library objects (`inherit LIB_AMBIANCE;` in core room objects).【F:mud/nightmare4/secure/include/lib.h†L4-L85】【F:mud/nightmare4/lib/std/room.c†L9-L25】
- SEFUN aggregation pulls dozens of helper functions through absolute-path `#include` statements and is annotated with `#pragma save_binary`, so the preprocessor must resolve quoted includes from the mudlib root as textual inserts rather than separate compilation units.【F:mud/nightmare4/secure/sefun/sefun.c†L7-L55】
- Core objects use modern LPC syntax features: typed arrays with the `array` keyword, `varargs` modifiers, multiple access qualifiers (`private static`), inheritance by macro-expanded paths, and lambda closures (`(: :)`) invoked via efuns like `call_out`.【F:mud/nightmare4/lib/std/room.c†L18-L116】【F:mud/nightmare4/secure/daemon/master.c†L31-L124】
- The mudlib defines lightweight `class` records with field lists and constructs them dynamically (`new(class comprehension)`), alongside function-typed fields and `functionp` checks, so parser and type handling must preserve class metadata and function pointer qualifiers.【F:mud/nightmare4/lib/language.c†L9-L88】
- Bootstrapping relies on a master object that reads preload lists from configuration files and schedules maintenance via closures, implying loader support for absolute object paths, configuration-driven preloads, and efuns like `read_file`, `call_out`, and `catch`/`error` behavior consistent with the driver expectations.【F:mud/nightmare4/secure/daemon/master.c†L66-L160】

## Proposed development path
### Preprocessing
1) Add configurable include search roots for `mud/nightmare4/secure/include`, `mud/nightmare4/include`, and `mud/nightmare4/lib/include`, honoring angle-bracket lookup order before local quotes.
2) Implement absolute-path include handling that anchors `/...` to the mudlib root and performs textual substitution for `.c` fragments (as in SEFUN), while ignoring or recording `#pragma save_binary`.
3) Seed predefined macros expected by the mudlib (`NightmareLPMud`, version identifiers from `global.h`, `__FILE__`, `__DIR__`) and ensure macro expansion applies to `inherit` arguments.

### Scanning
4) Extend the lexer to recognize LPC-specific keywords and modifiers (`varargs`, `nomask`, `nosave`, `static`, `private`, `protected`, `public`, `function`, `class`, `foreach`) and the `array` type keyword alongside `*` syntax.
5) Treat `(: ... :)` closures as first-class tokens, including `$1`-style placeholders and optional argument lists, without conflating them with parenthesized expressions.
6) Preserve mapping (`([ ])`) and array (`({ })`) literal delimiters distinctly to ease parser construction.

### Parsing
7) Support `inherit` clauses with macro-expanded string expressions, multiple modifiers on declarations, typed array forms (`object array x;` and `object *x;`), and forward declarations in headers.
8) Add grammar rules for LPC `class` definitions and member access (`instance->field`), `new(class X)` construction, and `function`-typed fields.
9) Handle lambda closures, `foreach` statement variants over arrays and mappings, and `varargs` parameter lists; ensure `catch` expressions integrate with exception handling semantics.

### Semantic analysis and compilation
10) Inline included `.c` content into the same compilation unit so SEFUNs contribute functions visible at compile time; track originating file metadata for diagnostics.
11) Resolve `inherit` targets after macro and include processing, normalizing absolute paths and deduplicating shared bases.
12) Model class layouts and function pointer flags (e.g., `FP_OWNER_DESTED`) so type checking and runtime invocation preserve mudlib expectations; accommodate multiple storage qualifiers (`private static`, etc.).

### Loading and runtime integration
13) Establish an object path resolver that maps leading-`/` mudlib paths to compiled LPC2J artifacts, with sefun and master objects initialized before general loads.
14) Implement or stub required efuns visible in the mudlib inspection (`call_out`, `read_file`, `write`, `error`, `functionp`, `evaluate`, mapping/array constructors), ensuring semantics match the driver used by Nightmare IV.
15) Wire bootstrapping to honor `CFG_PRELOAD` and related config files, invoking master hooks (`epilog`, `preload`, privilege resolution) so mudlib daemons and verbs can load in the expected order.

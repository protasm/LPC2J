# Unary negation type handling

## Problem
Unary negation previously assumed the operand was always an `int`. Any LPC expression whose inferred type was `status`, `float`, or `mixed` caused the compiler to throw an `IllegalStateException` during type resolution, producing stack traces like:

```
java.lang.IllegalStateException: Unary '-' operator requires an integer operand.
```

In practice, LPC code frequently applies `-` to values that are effectively numeric but not strictly typed as `int`, such as `status` results or mixed-typed locals. This surfaced when loading the sample torch object, whose heartbeat dims the room with a negative light adjustment:

```
    set_light(-1);
```

The same file also passes a negative weight adjustment back to the player:

```
    if (call_other(ob, "query_level"))
        call_other(ob, "add_weight", -weight);
```

Both snippets come directly from `lpc2j.test.lpmud.obj.torch` and reflect common LPC idioms that should compile without crashing the toolchain.

## Change
Unary negation now accepts any numeric-compatible operand: `int`, `float`, `status`, and `mixed` (as well as operands whose type is still unresolved). The operator returns `float` when negating a float and `int` otherwise, while still rejecting clearly non-numeric operands with a descriptive error.

## Examples
The following snippets now compile successfully:

```lpc
// Negating a status result
status ok = 1;
return -ok;
```

```lpc
// Negating a float temporary
float drift;
drift = -drift;
```

```lpc
// Negating a mixed-typed value (e.g., call_other result)
mixed delta = call_other("/obj/meter", "reading");
if (delta)
    return -delta;
```

These cases previously triggered an exception during compilation; after the change, they produce the expected numeric negation.

# Handling implicit `null` returns for untyped LPC functions

## Problem
When an untyped LPC function ends without an explicit return value, the
compiler infers the method's return type as `LPCNULL`. During bytecode
emission the compiler always appends a default return instruction to ensure the
method terminates. However, the default-return helper did not recognize
`LPCNULL` and threw `UnsupportedOperationException: Unsupported implicit return
 type: LPCNULL` while compiling affected functions.

## Fix
The bytecode generator now treats `LPCNULL` the same as other reference types
and emits `aconst_null` followed by `areturn` for implicit returns. This keeps
the generated method well-formed even when inference concludes that the
function's return type is `null`.

## LPC snippets addressed
Functions that omit an explicit return value now compile successfully. Typical
cases include guards that fall through or early exits without values:

```lpc
foo() {
    if (!ready)
        return; // no value
    // ... more work ...
}
```

A similar pattern appears when the compiler infers that a function's return
value is effectively `null`:

```lpc
bar() {
    if (should_abort())
        return 0; // treated as null/false
    // other logic
}
```

Both scenarios now receive an implicit `null` return that matches the inferred
`LPCNULL` signature instead of crashing the compiler.

## Exact code from `lpc2j.test.lpmud.obj.torch`
The crash surfaced while compiling the untyped torch object's heartbeat
function. The function can fall through without an explicit return value,
causing the inferred return type to be `LPCNULL`:

```lpc
heart_beat() {
    object ob;
    if (!is_lit)
        return;
    amount_of_fuel -= 1;
    if (amount_of_fuel > 0)
        return;
    say(name + " goes dark.\n");
    set_heart_beat(0);
    is_lit = 0;
    set_light(-1);
    ob = environment();
    if (call_other(ob, "query_level"))
        call_other(ob, "add_weight", -weight);
    destruct(this_object());
}
```

With the updated implicit-return handling, this function now compiles without
throwing `UnsupportedOperationException` and produces valid bytecode.

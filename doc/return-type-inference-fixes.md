# Return type inference fixes for untyped LPC functions

## Overview
Java class verification failed when loading translated LPC objects whose untyped
functions returned primitive literals. The compiler generated JVM methods with
`java/lang/Object` return descriptors for these functions, but emitted
`IRETURN` instructions because it still recognized the literal values as
integers. The mismatch produced `java.lang.VerifyError: Bad return type`
failures when loading the generated classes.

This change teaches the semantic type checker to record the return type of each
function based on the expressions it actually returns. The inferred type feeds the
method symbol used during bytecode generation, ensuring that the JVM method
signature and the emitted return opcode agree.

## What changed
- The semantic type checker now tracks the method currently being visited and
  updates that method's symbol type whenever it encounters an explicit `return`
  statement.
- Method return descriptors produced during bytecode generation now reflect the
  inferred type (e.g., `int` instead of `java/lang/Object`), preventing
  verifier errors for primitive return values.

## LPC snippets affected
Untyped LPC functions that return primitive values now receive accurate return
signatures. For example:

```lpc
get() { return 1; }
```

Previously, this translated to a JVM method returning `Object` while emitting
`IRETURN`, triggering a verifier failure. After this change the method is
correctly declared to return an `int`.

Another common pattern—guarded initialization with an early return—also benefits
from the updated inference:

```lpc
reset(arg) {
    if (arg)
        return;
    // initialization follows...
}
```

The early `return;` now leaves the method's inferred return type untouched while
return statements with values still inform the signature.

## Exact code from `lpc2j.test.lpmud.obj.torch`
The fix addresses verification failures when compiling the following functions
from `src/io/github/protasm/lpc2j/test/lpmud/obj/torch.c`:

```lpc
reset(arg) {
    if (arg)
        return;
    amount_of_fuel = 0; name = 0; is_lit = 0; weight = 0;
}

get() { return 1; }
```

With corrected inference, the generated `reset` and `get` methods now have
matching JVM signatures and return opcodes, so the torch object loads without
verification errors.

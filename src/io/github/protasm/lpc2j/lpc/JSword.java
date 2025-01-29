package io.github.protasm.lpc2j.lpc;

import io.github.protasm.lpc2j.LPCObject;

class JSword extends LPCObject {
    int x = 101;

    int foo() {
	x = 1;

	return x;
    }
}

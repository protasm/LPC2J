package io.github.protasm.lpc2j.lpc;

import obj.weapon.sword;

class Master {
    public static void main(String... args) {
	sword sword = new sword();
	int x = Integer.parseInt(args[0]);

	System.out.println(sword.fib(x));
    }
}

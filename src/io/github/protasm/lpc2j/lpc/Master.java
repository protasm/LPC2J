package io.github.protasm.lpc2j.lpc;

import obj.weapon.sword;
import obj.weapon.armor;

class Master {
    public static void main(String... args) {
	sword sword = new sword();
        armor armor = new armor();

        System.out.println(sword.getX());
        armor.bar(sword);
        System.out.println(sword.getX());
    }
}

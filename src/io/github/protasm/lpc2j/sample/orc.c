/* 
 * orc.c
 *
 * A self-contained LPC object used for compiler and semantic-analysis testing.
 * No includes or inherits.
 */

int strength;
int health;
string name;
string clan;
status aggressive;

/* Object initialization */
void create() {
    strength = 15;
    health = 100;
    name = "Gruk";
    clan = "Bloodfang";
    aggressive = 1;
}

/* Simple getters */
int query_strength() {
    return strength;
}

string query_name() {
    return name;
}

status is_aggressive() {
    return aggressive;
}

/* Mutators */
void set_aggressive(status flag) {
    aggressive = flag;
}

void take_damage(int amount) {
    health -= amount;
    if (health < 0) {
        health = 0;
    }
}

/* Demonstrates string manipulation and efun usage */
string describe() {
    string desc;

    desc = "An orc named " + name;
    desc += " of the " + clan + " clan.";

    if (aggressive) {
        desc += " It looks hostile.";
    } else {
        desc += " It seems calm.";
    }

    return desc;
}

/* Demonstrates efun calls and conditional logic */
status is_alive() {
    return health > 0;
}

/* Interaction with another object via -> operator */
void attack(object target) {
    int damage;

    if (!target) {
        write("The orc snarls at nothing.\n");
        return;
    }

    if (!is_alive()) {
        write(name + " is too dead to fight.\n");
        return;
    }

    damage = strength + random(5);

    write(name + " attacks viciously!\n");

    target->take_damage(damage);

    if (!target->is_alive()) {
        write("The orc has slain its foe!\n");
    }
}

/* Demonstrates efuns returning objects */
void shout_for_help() {
    object room;

    room = environment(this_object());

    if (room) {
        write(name + " bellows a savage war cry!\n");
        room->hear_shout(name + " calls for reinforcements!");
    }
}

/* Void method with side effects only */
void heal_self() {
    if (health < 100) {
        health += 10;
        if (health > 100) {
            health = 100;
        }
    }
}

/* Mixed return types and efun use */
int compare_strength(object other) {
    if (!other) {
        return 0;
    }

    return strength - other->query_strength();
}


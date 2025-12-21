/* ============================================================
 *  Rusty Sword — LPC2J Semantic & IR Stress Test
 *
 *  This object intentionally mixes:
 *   - typed and untyped functions
 *   - typed and untyped parameters
 *   - explicit and implicit returns
 *   - legacy implicit return 0 behavior
 *   - preprocessor macros
 *
 *  No inherits. No includes. Pure LPC.
 * ============================================================
 */

/* ---------- Preprocessor Macros ---------- */

#define BASE_DMG        7
#define RUST_PENALTY    2
#define BONUS(x)        ((x) / 2)
#define ZERO()          0
#define NAME            "a rusty sword"

/* ---------- Global State ---------- */

int durability = 10;
string short_desc = NAME;

/* ---------- Typed Function, Explicit Return ---------- */

int query_damage() {
    return BASE_DMG - RUST_PENALTY;
}

/* ---------- Typed Function, Implicit Return (fallthrough) ---------- */
/* Should implicitly return 0 */

int query_bonus() {
    int b;
    b = BONUS(durability);
    /* no return */
}

/* ---------- Typed Parameters, Explicit Return ---------- */

int compute_damage(int strength) {
    return query_damage() + BONUS(strength);
}

/* ---------- Typed Function, Explicit return; ---------- */
/* Runtime still returns 0 */

void wear_down() {
    durability -= 1;
    if (durability < 0) {
        durability = 0;
    }
    return;
}

/* ---------- Untyped Function, Untyped Parameter ---------- */

hit(target) {
    int dmg;
    dmg = compute_damage(target->query_str());
    target->take_damage(dmg);
    wear_down();
    /* implicit return 0 */
}

/* ---------- Untyped Function, Explicit Return With Value ---------- */

status is_broken() {
    if (durability <= ZERO()) {
        return 1;
    }
    return 0;
}

/* ---------- Mixed Typed / Untyped Parameters ---------- */

int can_wield(who, int skill) {
    if (skill < 5) {
        return 0;
    }
    who->write("You wield " + short_desc + ".\n");
    return 1;
}

/* ---------- Untyped Function Using Macros ---------- */

short() {
    return short_desc;
}

/* ---------- Typed Function Returning Macro ---------- */

int query_zero() {
    return ZERO();
}

/* ---------- Partial Return Paths ---------- */
/* One path returns, one falls through */

int risky_strike(int chance) {
    if (chance > 50) {
        return compute_damage(chance);
    }
    /* implicit return 0 */
}

/* ---------- End of File ---------- */


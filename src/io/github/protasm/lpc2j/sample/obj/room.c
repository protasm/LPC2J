/* ============================================================
 *  /obj/room.c
 *
 *  Sample room object for LPC2J
 * ============================================================
 */

/* ---------- Includes ---------- */

#include <room_defs.h>      /* system include */
#include "room_macros.h"    /* local include */

/* ---------- Room State ---------- */

int room_type = ROOM_INDOORS;
int light_level = DEFAULT_LIGHT;

string short_desc = SHORT_DESC;
string long_desc  = LONG_DESC;

/* ---------- Typed Function, Explicit Return ---------- */

int query_light() {
    return light_level;
}

/* ---------- Typed Function, Implicit Return ---------- */
/* Should implicitly return 0 */

int is_lit() {
    if (light_level > 0) {
        return 1;
    }
    /* fallthrough -> return 0 */
}

/* ---------- Untyped Function, Explicit Return ---------- */

short() {
    return short_desc;
}

/* ---------- Untyped Function, Implicit Return ---------- */

long() {
    write(long_desc);
    /* implicit return 0 */
}

/* ---------- Mixed Typed / Untyped Parameters ---------- */

int can_enter(who, string dir) {
    if (!VALID_EXIT(dir)) {
        who->write("You cannot go that way.\n");
        return CANNOT_ENTER;
    }

    who->write("You enter the room.\n");
    return CAN_ENTER;
}

/* ---------- Typed Function, Explicit return; ---------- */

void reset_room() {
    light_level = DEFAULT_LIGHT;
    return;
}

/* ---------- Partial Return Paths ---------- */

int room_flags(int test) {
    if (test > 0) {
        return ROOM_INDOORS;
    }
    /* implicit return 0 */
}

/* ---------- End of File ---------- */


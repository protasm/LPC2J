/* ------------------------------------------------------------
 *  /living/orc.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include <paths.h>
#include "npc_common.h"

void create() {
    set_identity("riverbank orc",
                 "A broad-shouldered orc posted near the river, watching travelers with a wary eye.");
    set_stats(BASE_HEALTH + 30, BASE_STAMINA - 5, BASE_ATTACK + 6);
    set_station(R_RIVERBANK);
}

status is_hostile() {
    return 1;
}

string battle_cry() {
    return "For the Bloodfork clan!";
}

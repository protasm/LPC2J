/* ------------------------------------------------------------
 *  /living/adventurer.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include <paths.h>
#include "npc_common.h"

void create() {
    set_identity("road-weary adventurer",
                 "A curious adventurer collecting stories from the guild hall before heading north.");
    set_stats(BASE_HEALTH + 10, BASE_STAMINA + 10, BASE_ATTACK + 2);
    set_station(R_GUILD_HALL);
}

status is_helpful() {
    return 1;
}

string greeting() {
    return "Care to share a tale before I cross the bridge?";
}

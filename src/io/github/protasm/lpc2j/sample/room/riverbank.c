/* ------------------------------------------------------------
 *  /room/riverbank.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include <paths.h>
#include "room_common.h"

void create() {
    room_type = ROOM_OUTDOORS;
    light_level = LIGHT_BRIGHT;

    short_desc = "Riverbank";
    long_desc =
"Reeds sway at the river's edge, whispering over stones.\n"
"Footprints show where patrols keep watch on the bridge overhead.\n";

    exits = ([
        "north": R_RIVER_BRIDGE,
        "west": R_WORKSHOP_LANE
    ]);

    items = ([
        OBJ_RIVER_STONE: "A smooth, palm-sized stone sits near the waterline.",
        "reeds": "Tall reeds bend as the current breathes beneath them."
    ]);

    npcs = ({ NPC_ORC });
}

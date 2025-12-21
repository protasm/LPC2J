/* ------------------------------------------------------------
 *  /room/south_square.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include <paths.h>
#include "room_common.h"

void create() {
    room_type = ROOM_OUTDOORS;
    light_level = LIGHT_BRIGHT;

    short_desc = "South Square";
    long_desc =
"A modest square where caravans form before departing.\n"
"Children chase each other around the central fountain, splashing in the spray.\n";

    exits = ([
        "north": R_NORTH_GATE,
        "east": R_CARAVAN_ROAD
    ]);

    items = ([
        OBJ_NOTICE_BOARD: "Pinned with ink-stained notes and fresh announcements.",
        "fountain": "A stone fountain gurgles with clean spring water."
    ]);

    npcs = ({ });
}

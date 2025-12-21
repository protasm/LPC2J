/* ------------------------------------------------------------
 *  /room/river_bridge.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include <paths.h>
#include "room_common.h"

void create() {
    room_type = ROOM_OUTDOORS;
    light_level = LIGHT_BRIGHT;

    short_desc = "River Bridge";
    long_desc =
"An arched bridge spans the slow-moving river.\n"
"Mist gathers under the beams while fishers trade news from the bank.\n";

    exits = ([
        "west": R_SCHOLARS_GARDEN,
        "south": R_RIVERBANK
    ]);

    items = ([
        OBJ_BRIDGE_ROPE: "Coils of rope sit ready to secure the railings in foul weather.",
        "river": "The river glints as it winds eastward toward distant marshes."
    ]);

    npcs = ({ });
}

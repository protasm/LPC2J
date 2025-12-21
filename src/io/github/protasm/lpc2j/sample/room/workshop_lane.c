/* ------------------------------------------------------------
 *  /room/workshop_lane.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include <paths.h>
#include "room_common.h"

void create() {
    room_type = ROOM_OUTDOORS;
    light_level = LIGHT_DIM;

    short_desc = "Workshop Lane";
    long_desc =
"A narrow lane echoes with the ring of chisels and saws.\n"
"Canvas awnings stretch overhead, shading workbenches from sun and rain.\n";

    exits = ([
        "north": R_SCHOLARS_GARDEN,
        "west": R_GUILD_HALL,
        "east": R_RIVERBANK
    ]);

    items = ([
        OBJ_TOOL_RACK: "Tools hang in neat rows, cared for by the guild artisans.",
        "awnings": "Sturdy canvas awnings ripple with the wind."
    ]);

    npcs = ({ });
}

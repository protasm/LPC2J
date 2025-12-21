/* ------------------------------------------------------------
 *  /room/caravan_road.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include <paths.h>
#include "room_common.h"

void create() {
    room_type = ROOM_OUTDOORS;
    light_level = LIGHT_BRIGHT;

    short_desc = "Caravan Road";
    long_desc =
"Wheel-ruts cut through the packed earth, pointing toward far markets.\n"
"Colorful pennants hang from poles, fluttering above the steady trade.\n";

    exits = ([
        "north": R_MARKET_STREET,
        "west": R_SOUTH_SQUARE,
        "east": R_GUILD_HALL
    ]);

    items = ([
        OBJ_ROAD_MAP: "A map tucked into a post box shows the guarded routes along the river.",
        "pennants": "Weathered pennants ripple and snap in the breeze."
    ]);

    npcs = ({ });
}

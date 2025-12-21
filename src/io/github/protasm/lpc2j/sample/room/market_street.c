/* ------------------------------------------------------------
 *  /room/market_street.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include <paths.h>
#include "room_common.h"

void create() {
    room_type = ROOM_OUTDOORS;
    light_level = LIGHT_BRIGHT;

    short_desc = "Market Street";
    long_desc =
"Stalls line both sides of the narrow street, laden with spices and cloth.\n"
"Lanterns sway overhead, guiding the way toward the temple steps.\n";

    exits = ([
        "west": R_NORTH_GATE,
        "east": R_TEMPLE_STEPS,
        "south": R_CARAVAN_ROAD
    ]);

    items = ([
        OBJ_MARKET_CRATE: "Crates of trade goods wait to be inventoried.",
        "lantern": "Paper lanterns sway gently, casting warm circles of light."
    ]);

    npcs = ({ });
}
